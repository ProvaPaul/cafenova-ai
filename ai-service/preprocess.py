"""
Data Preprocessing Script — Groceries Dataset
=============================================

Loads the raw Groceries_dataset.csv, validates, groups into transactions,
encodes into a one-hot DataFrame, and saves a summary report.

Run from the ai-service directory:
    python preprocess.py [--dataset PATH]
"""

import argparse
import json
import os
import sys
import time
from pathlib import Path
from typing import Any

import pandas as pd

# ── Constants ─────────────────────────────────────────────────────────────────
DEFAULT_DATASET = Path(__file__).parent.parent / "archive" / "Groceries_dataset.csv"
OUTPUT_DIR      = Path(__file__).parent / "data"


def log(msg: str) -> None:
    print(f"  {msg}", flush=True)


# ── Step 1: Load & Validate ───────────────────────────────────────────────────

def load_and_validate(path: Path) -> pd.DataFrame:
    print("\n[1] Loading & validating dataset...")
    if not path.exists():
        print(f"  ERROR: Dataset not found at {path}")
        sys.exit(1)

    df = pd.read_csv(path)
    log(f"Raw rows loaded: {len(df):,}")

    # Validate expected columns
    expected = {"Member_number", "Date", "itemDescription"}
    missing_cols = expected - set(df.columns)
    if missing_cols:
        print(f"  ERROR: Missing columns: {missing_cols}")
        sys.exit(1)

    # Missing value report
    null_counts = df.isnull().sum()
    if null_counts.any():
        log(f"Missing values found:\n{null_counts[null_counts > 0]}")
        df = df.dropna(subset=["Member_number", "Date", "itemDescription"])
        log(f"Rows after dropping nulls: {len(df):,}")
    else:
        log("No missing values found.")

    # Normalise
    df["itemDescription"] = df["itemDescription"].str.strip().str.lower()
    df["Member_number"]   = df["Member_number"].astype(str).str.strip()
    df["Date"]            = pd.to_datetime(df["Date"], format="%d-%m-%Y", errors="coerce")

    invalid_dates = df["Date"].isna().sum()
    if invalid_dates:
        log(f"Dropping {invalid_dates} rows with unparseable dates.")
        df = df.dropna(subset=["Date"])

    log(f"Clean rows: {len(df):,}")
    return df


# ── Step 2: Transaction Grouping ──────────────────────────────────────────────

def group_transactions(df: pd.DataFrame) -> list[list[str]]:
    """
    Group by Member_number — treat each member's full purchase history as one basket.

    Why member-level instead of (member, date):
    The date-level approach produces baskets of only 2–3 items on average,
    generating too few meaningful co-occurrence patterns. Member-level grouping
    produces baskets of ~9 unique items, giving the Apriori algorithm enough
    co-occurrence signal to generate high-quality rules (lift > 1.4).

    When this model is replaced with our own Cafe Orders data, we will group by
    order_id (one visit = one transaction) since those baskets will be richer.
    """
    print("\n[2] Grouping into transactions (by Member_number — full purchase history)...")
    grouped = (
        df.groupby("Member_number")["itemDescription"]
        .apply(lambda x: sorted(set(x)))  # unique items per member, sorted
        .tolist()
    )
    transactions = [t for t in grouped if len(t) >= 2]
    log(f"Total member baskets (>=2 unique items): {len(transactions):,}")
    log(f"Avg unique items per member: {sum(len(t) for t in transactions) / len(transactions):.2f}")
    return transactions


# ── Step 3: One-hot Encoding ──────────────────────────────────────────────────

def encode_transactions(transactions: list[list[str]]) -> pd.DataFrame:
    print("\n[3] One-hot encoding transactions...")
    from mlxtend.preprocessing import TransactionEncoder

    te = TransactionEncoder()
    te_array = te.fit_transform(transactions)
    encoded = pd.DataFrame(te_array, columns=te.columns_)
    log(f"Encoded shape: {encoded.shape[0]:,} baskets × {encoded.shape[1]} items")

    # Memory optimisation: bool → reduces RAM roughly 8× vs float64
    # (mlxtend already outputs bool dtype, but be explicit)
    encoded = encoded.astype(bool)
    ram_mb = encoded.memory_usage(deep=True).sum() / 1024 / 1024
    log(f"Encoded DataFrame RAM: {ram_mb:.1f} MB")
    return encoded


# ── Step 4: Dataset Statistics ────────────────────────────────────────────────

def compute_statistics(
    df_raw: pd.DataFrame,
    transactions: list[list[str]],
    encoded: pd.DataFrame,
) -> dict[str, Any]:
    print("\n[4] Computing dataset statistics...")

    item_freq = df_raw["itemDescription"].value_counts()
    support_series = encoded.mean().sort_values(ascending=False)

    stats: dict[str, Any] = {
        "total_rows":          int(len(df_raw)),
        "total_transactions":  len(transactions),
        "unique_items":        int(encoded.shape[1]),
        "avg_basket_size":     round(sum(len(t) for t in transactions) / len(transactions), 3),
        "date_range": {
            "start": str(df_raw["Date"].min().date()),
            "end":   str(df_raw["Date"].max().date()),
        },
        "top_10_items": [
            {"item": item, "count": int(count), "support": round(float(support_series.get(item, 0)), 4)}
            for item, count in item_freq.head(10).items()
        ],
        "support_stats": {
            "max":    round(float(support_series.max()), 4),
            "min":    round(float(support_series.min()), 4),
            "mean":   round(float(support_series.mean()), 4),
            "median": round(float(support_series.median()), 4),
        },
    }

    for k, v in stats.items():
        if isinstance(v, dict):
            for kk, vv in v.items():
                log(f"{k}.{kk}: {vv}")
        elif isinstance(v, list):
            log(f"{k}: [{v[0]['item']} ({v[0]['support']:.3f}), ...]")
        else:
            log(f"{k}: {v}")

    return stats


# ── Step 5: Save outputs ──────────────────────────────────────────────────────

def save_outputs(
    encoded: pd.DataFrame,
    transactions: list[list[str]],
    stats: dict[str, Any],
) -> None:
    print("\n[5] Saving preprocessed data...")
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    # Save encoded as Parquet (compact, fast to reload)
    encoded_path = OUTPUT_DIR / "encoded_transactions.parquet"
    encoded.to_parquet(encoded_path, index=False)
    size_kb = encoded_path.stat().st_size / 1024
    log(f"Saved encoded transactions: {encoded_path.name}  ({size_kb:.0f} KB)")

    # Save raw transactions list as JSON (used by training script)
    tx_path = OUTPUT_DIR / "transactions.json"
    with open(tx_path, "w", encoding="utf-8") as f:
        json.dump(transactions, f, separators=(",", ":"))
    log(f"Saved transactions list: {tx_path.name}")

    # Save statistics
    stats_path = OUTPUT_DIR / "dataset_stats.json"
    with open(stats_path, "w", encoding="utf-8") as f:
        json.dump(stats, f, indent=2)
    log(f"Saved dataset statistics: {stats_path.name}")


# ── Main ──────────────────────────────────────────────────────────────────────

def main() -> None:
    parser = argparse.ArgumentParser(description="Groceries dataset preprocessor")
    parser.add_argument("--dataset", default=str(DEFAULT_DATASET), help="Path to Groceries_dataset.csv")
    args = parser.parse_args()

    print("=" * 55)
    print("  SmartCafe AI — Data Preprocessing")
    print("  Dataset: Groceries (Market Basket Analysis)")
    print("=" * 55)
    t0 = time.time()

    df_raw      = load_and_validate(Path(args.dataset))
    transactions = group_transactions(df_raw)
    encoded     = encode_transactions(transactions)
    stats       = compute_statistics(df_raw, transactions, encoded)
    save_outputs(encoded, transactions, stats)

    elapsed = time.time() - t0
    print(f"\n  Preprocessing complete in {elapsed:.1f}s")
    print(f"  Output directory: {OUTPUT_DIR.resolve()}")
    print("=" * 55)


if __name__ == "__main__":
    main()
