"""
Train Apriori model from the MySQL database.

Pipeline:
    MySQL (orders + order_items + menu_items, status=COMPLETED)
    → transaction list
    → one-hot encoding
    → Apriori frequent itemsets
    → association rules
    → versioned model file (data/models/)
    → data/trained_rules.joblib  (latest — loaded by ai_engine.py)
    → data/metadata.json         (latest)
    → ai_training_sessions table (log entry)

Run standalone:
    cd ai-service
    python -m scripts.train_from_db

Or called from FastAPI admin endpoint.
"""

from __future__ import annotations

import gc
import json
import shutil
import sys
import time
from datetime import datetime
from pathlib import Path

import joblib
import pandas as pd

sys.path.insert(0, str(Path(__file__).parent.parent))

from app.config import get_settings
from app.db.connector import get_db, test_connection
from app.db.transaction_builder import load_transactions

DATA_DIR   = Path(__file__).parent.parent / "data"
MODELS_DIR = DATA_DIR / "models"
RULES_PATH = DATA_DIR / "trained_rules.joblib"
META_PATH  = DATA_DIR / "metadata.json"


def _detect_source() -> str:
    """Detect whether current DB has demo or real orders."""
    try:
        with get_db() as conn:
            cur = conn.cursor()
            cur.execute("SELECT COUNT(*) FROM orders WHERE order_number LIKE 'DMO-%' AND status='COMPLETED'")
            demo_cnt = cur.fetchone()[0]
            cur.execute("SELECT COUNT(*) FROM orders WHERE order_number NOT LIKE 'DMO-%' AND status='COMPLETED'")
            real_cnt = cur.fetchone()[0]
            cur.close()
        if real_cnt > demo_cnt:
            return "CAFE_ORDERS"
        if demo_cnt > 0:
            return "DEMO_DATA"
        return "CAFE_ORDERS"
    except Exception:
        return "CAFE_ORDERS"


def _log_session(session_data: dict) -> int:
    """Insert a training session record; return the new ID."""
    try:
        with get_db() as conn:
            cur = conn.cursor()
            cur.execute(
                """INSERT INTO ai_training_sessions
                   (version, model_file, dataset_source,
                    n_transactions, n_unique_items, n_frequent_itemsets, n_rules,
                    min_support, min_confidence, avg_confidence, avg_lift, max_lift,
                    training_time_sec, status, notes)
                   VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)""",
                (
                    session_data["version"],
                    session_data["model_file"],
                    session_data["dataset_source"],
                    session_data["n_transactions"],
                    session_data["n_unique_items"],
                    session_data["n_frequent_itemsets"],
                    session_data["n_rules"],
                    session_data["min_support"],
                    session_data["min_confidence"],
                    session_data.get("avg_confidence", 0.0),
                    session_data.get("avg_lift", 0.0),
                    session_data.get("max_lift", 0.0),
                    session_data["training_time_sec"],
                    session_data["status"],
                    session_data.get("notes", ""),
                ),
            )
            sid = cur.lastrowid
            cur.close()
        return sid
    except Exception:
        return -1


def _update_session_status(sid: int, status: str, error: str | None = None) -> None:
    if sid < 0:
        return
    try:
        with get_db() as conn:
            cur = conn.cursor()
            cur.execute(
                "UPDATE ai_training_sessions SET status=%s, error_message=%s WHERE id=%s",
                (status, error, sid),
            )
            cur.close()
    except Exception:
        pass


def train() -> dict:
    """
    Full training pipeline.

    Returns a summary dict:
        {success, version, n_transactions, n_items, n_rules,
         avg_confidence, avg_lift, training_time_sec, message}
    """
    settings = get_settings()

    if not test_connection():
        return {"success": False, "message": "Cannot connect to MySQL. Check DB settings."}

    t_total = time.time()

    # ── 1. Load transactions from DB ──────────────────────────────────────────
    print("Loading transactions from database...")
    try:
        transactions, db_stats = load_transactions()
    except Exception as e:
        return {"success": False, "message": f"Failed to load transactions: {e}"}

    n_trans = db_stats["n_transactions"]
    n_items = db_stats["n_unique_items"]
    print(f"  {n_trans} transactions, {n_items} unique items")

    if n_trans < 10:
        return {
            "success": False,
            "message": (
                f"Only {n_trans} completed orders found. "
                "Generate demo data first or wait for more real orders."
            ),
        }

    # ── 2. Encode ─────────────────────────────────────────────────────────────
    from mlxtend.preprocessing import TransactionEncoder
    te = TransactionEncoder()
    encoded = pd.DataFrame(te.fit_transform(transactions), columns=te.columns_)
    print(f"  Encoded: {encoded.shape[0]} x {encoded.shape[1]}")

    # ── 3. Apriori ────────────────────────────────────────────────────────────
    from mlxtend.frequent_patterns import apriori, association_rules

    min_support    = settings.train_min_support
    min_confidence = settings.train_min_confidence
    max_len        = settings.train_max_len

    # Adaptive min_support: if few transactions, lower it
    if n_trans < 200:
        min_support = max(0.005, min_support * 0.5)
        print(f"  Low transaction count — using min_support={min_support:.3f}")

    t_train = time.time()
    print(f"  Running Apriori (min_support={min_support}, max_len={max_len})...")
    freq_items = apriori(
        encoded,
        min_support=min_support,
        use_colnames=True,
        max_len=max_len,
    )
    del encoded
    gc.collect()

    n_freq = len(freq_items)
    print(f"  {n_freq} frequent itemsets found")

    if n_freq == 0:
        return {
            "success": False,
            "message": f"No frequent itemsets found at min_support={min_support}. "
                       "Try generating more orders.",
        }

    rules = association_rules(freq_items, metric="confidence", min_threshold=min_confidence)
    rules = rules.sort_values("lift", ascending=False).reset_index(drop=True)
    training_time = round(time.time() - t_train, 3)
    print(f"  {len(rules)} association rules in {training_time}s")

    # ── 4. Version + save model ───────────────────────────────────────────────
    source    = _detect_source()
    ts_str    = datetime.utcnow().strftime("%Y%m%d_%H%M%S")
    version   = f"cafe_v{ts_str}"
    MODELS_DIR.mkdir(parents=True, exist_ok=True)
    model_file = MODELS_DIR / f"{version}_rules.joblib"

    joblib.dump(rules, model_file, compress=3)
    shutil.copy(model_file, RULES_PATH)   # always keep latest at fixed path

    # Compute summary stats
    avg_conf = round(float(rules["confidence"].mean()), 4) if len(rules) else 0.0
    avg_lift = round(float(rules["lift"].mean()), 4)       if len(rules) else 0.0
    max_lift = round(float(rules["lift"].max()), 4)        if len(rules) else 0.0

    metadata = {
        "version":                version,
        "dataset_source":         source,
        "algorithm":              "apriori",
        "n_transactions":         n_trans,
        "n_unique_items":         n_items,
        "n_frequent_itemsets":    n_freq,
        "n_rules":                len(rules),
        "min_support":            min_support,
        "min_confidence":         min_confidence,
        "max_len":                max_len,
        "avg_confidence":         avg_conf,
        "avg_lift":               avg_lift,
        "max_lift":               max_lift,
        "training_time_seconds":  training_time,
        "trained_at":             datetime.utcnow().isoformat(),
        "note": (
            "Trained on cafe demo data. Replace with real orders for production."
            if source == "DEMO_DATA"
            else "Trained on real cafe order data."
        ),
        "dataset": (
            "Cafe Demo Dataset (generated)"
            if source == "DEMO_DATA"
            else "Cafe Orders Database"
        ),
    }
    META_PATH.write_text(json.dumps(metadata, indent=2), encoding="utf-8")

    # ── 5. Log to DB ──────────────────────────────────────────────────────────
    sid = _log_session({
        "version":           version,
        "model_file":        str(model_file.relative_to(DATA_DIR.parent)),
        "dataset_source":    source,
        "n_transactions":    n_trans,
        "n_unique_items":    n_items,
        "n_frequent_itemsets": n_freq,
        "n_rules":           len(rules),
        "min_support":       min_support,
        "min_confidence":    min_confidence,
        "avg_confidence":    avg_conf,
        "avg_lift":          avg_lift,
        "max_lift":          max_lift,
        "training_time_sec": training_time,
        "status":            "COMPLETED",
        "notes":             f"Trained from {source}",
    })

    total_time = round(time.time() - t_total, 3)

    return {
        "success":            True,
        "version":            version,
        "dataset_source":     source,
        "n_transactions":     n_trans,
        "n_unique_items":     n_items,
        "n_frequent_itemsets": n_freq,
        "n_rules":            len(rules),
        "avg_confidence":     avg_conf,
        "avg_lift":           avg_lift,
        "max_lift":           max_lift,
        "training_time_sec":  training_time,
        "total_time_sec":     total_time,
        "model_file":         str(model_file.name),
        "session_id":         sid,
        "message": (
            f"Training complete in {total_time}s. "
            f"{len(rules)} rules from {n_trans} orders. "
            f"Model: {version}. Reload the AI engine to use the new model."
        ),
    }


if __name__ == "__main__":
    print("Training AI model from database...")
    result = train()
    print(json.dumps(result, indent=2))
