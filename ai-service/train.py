"""
Model Training Script — Apriori Association Rules
==================================================

Trains association rules from preprocessed transaction data.
Saves trained_rules.joblib and metadata.json to ai-service/data/.

Laptop-friendly defaults:
  min_support   = 0.02
  min_confidence = 0.50
  max_len        = 3

Run from the ai-service directory:
    python train.py [--support 0.02] [--confidence 0.50] [--max-len 3]
"""

import argparse
import gc
import json
import time
from pathlib import Path

# ── Paths ─────────────────────────────────────────────────────────────────────
DATA_DIR = Path(__file__).parent / "data"


def log(msg: str) -> None:
    print(f"  {msg}", flush=True)


# ── Training ──────────────────────────────────────────────────────────────────

def run_training(min_support: float, min_confidence: float, max_len: int) -> None:
    import joblib
    import pandas as pd
    from mlxtend.frequent_patterns import apriori, association_rules

    print("=" * 55)
    print("  SmartCafe AI — Model Training")
    print("  Algorithm: Apriori + Association Rules")
    print("=" * 55)
    t0 = time.time()

    # ── Load encoded transactions ─────────────────────────────────────────────
    encoded_path = DATA_DIR / "encoded_transactions.parquet"
    if not encoded_path.exists():
        print(f"  ERROR: Run preprocess.py first. Missing: {encoded_path}")
        return

    print("\n[1] Loading encoded transactions...")
    encoded = pd.read_parquet(encoded_path)
    log(f"Shape: {encoded.shape[0]:,} baskets × {encoded.shape[1]} items")

    # ── Apriori: frequent itemsets ────────────────────────────────────────────
    print(f"\n[2] Running Apriori (min_support={min_support}, max_len={max_len})...")
    t1 = time.time()
    frequent_itemsets = apriori(
        encoded,
        min_support=min_support,
        use_colnames=True,
        max_len=max_len,
        verbose=0,
    )
    # Free the large encoded DataFrame as soon as Apriori finishes
    del encoded
    gc.collect()

    n_itemsets = len(frequent_itemsets)
    log(f"Frequent itemsets found: {n_itemsets:,}  ({time.time()-t1:.1f}s)")

    if n_itemsets == 0:
        print("  ERROR: No frequent itemsets found. Lower min_support and retry.")
        return

    # ── Association rules ─────────────────────────────────────────────────────
    print(f"\n[3] Generating association rules (min_confidence={min_confidence})...")
    t2 = time.time()
    rules = association_rules(
        frequent_itemsets,
        metric="confidence",
        min_threshold=min_confidence,
    )
    n_rules = len(rules)
    log(f"Rules generated: {n_rules:,}  ({time.time()-t2:.1f}s)")

    if n_rules == 0:
        print("  ERROR: No rules generated. Lower min_confidence and retry.")
        return

    # ── Clean up rules DataFrame ──────────────────────────────────────────────
    # Convert frozensets to sorted tuple strings for JSON-serialisable storage
    rules["antecedents"] = rules["antecedents"].apply(lambda x: sorted(x))
    rules["consequents"] = rules["consequents"].apply(lambda x: sorted(x))

    # Round floats to 4 decimal places to reduce file size
    for col in ["support", "confidence", "lift", "leverage", "conviction"]:
        if col in rules.columns:
            rules[col] = rules[col].round(4)

    # Sort by lift descending — highest-quality rules first
    rules = rules.sort_values("lift", ascending=False).reset_index(drop=True)

    # ── Rule quality summary ──────────────────────────────────────────────────
    print("\n[4] Rule quality summary...")
    log(f"Avg confidence: {rules['confidence'].mean():.3f}")
    log(f"Avg lift:       {rules['lift'].mean():.3f}")
    log(f"Avg support:    {rules['support'].mean():.4f}")
    log(f"Max lift:       {rules['lift'].max():.3f}")

    print("\n  Top 5 rules by lift:")
    for _, r in rules.head(5).iterrows():
        ant = " + ".join(r["antecedents"])
        con = " + ".join(r["consequents"])
        print(f"    {ant} => {con}  "
              f"conf={r['confidence']:.2f}  lift={r['lift']:.2f}  sup={r['support']:.4f}")

    # ── Save ──────────────────────────────────────────────────────────────────
    print("\n[5] Saving model artifacts...")
    DATA_DIR.mkdir(parents=True, exist_ok=True)

    rules_path = DATA_DIR / "trained_rules.joblib"
    joblib.dump(rules, rules_path, compress=3)
    size_kb = rules_path.stat().st_size / 1024
    log(f"Saved rules: {rules_path.name}  ({size_kb:.0f} KB)")

    elapsed = time.time() - t0
    metadata = {
        "algorithm":      "apriori",
        "dataset":        "Groceries Dataset (public — Market Basket Analysis)",
        "min_support":    min_support,
        "min_confidence": min_confidence,
        "max_len":        max_len,
        "n_frequent_itemsets": int(n_itemsets),
        "n_rules":        int(n_rules),
        "avg_confidence": round(float(rules["confidence"].mean()), 4),
        "avg_lift":       round(float(rules["lift"].mean()), 4),
        "avg_support":    round(float(rules["support"].mean()), 6),
        "training_time_seconds": round(elapsed, 2),
        "trained_at":     time.strftime("%Y-%m-%dT%H:%M:%S"),
        "model_file":     str(rules_path.resolve()),
        "note": (
            "Trained on public Groceries dataset for validation. "
            "Will be replaced by a model trained on Cafe Orders and OrderItems tables."
        ),
    }
    meta_path = DATA_DIR / "metadata.json"
    with open(meta_path, "w", encoding="utf-8") as f:
        json.dump(metadata, f, indent=2)
    log(f"Saved metadata: {meta_path.name}")

    print(f"\n  Training complete in {elapsed:.1f}s")
    print("=" * 55)


# ── Main ──────────────────────────────────────────────────────────────────────

def main() -> None:
    parser = argparse.ArgumentParser(description="Train Apriori association rules")
    parser.add_argument("--support",    type=float, default=0.02,  help="min_support (default: 0.02)")
    parser.add_argument("--confidence", type=float, default=0.30,  help="min_confidence (default: 0.30)")
    parser.add_argument("--max-len",    type=int,   default=3,     help="max itemset length (default: 3)")
    args = parser.parse_args()
    run_training(args.support, args.confidence, args.max_len)


if __name__ == "__main__":
    main()
