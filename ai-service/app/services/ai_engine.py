"""
AI Recommendation Engine — Apriori-based (Groceries dataset validation model).

This engine:
  1. Loads trained_rules.joblib on first use (lazy singleton).
  2. Given a list of input items, finds all rules whose antecedents
     are a subset of those items.
  3. Returns the best consequents ranked by lift then confidence.

Future upgrade path:
  Replace load_model() to load a model trained on our own Cafe Orders /
  OrderItems tables. The public interface (recommend, top_rules, explain)
  stays identical — no callers change.
"""

from __future__ import annotations

import json
import time
from functools import lru_cache
from pathlib import Path
from typing import Any

import joblib
import pandas as pd

DATA_DIR = Path(__file__).parent.parent.parent / "data"
RULES_PATH = DATA_DIR / "trained_rules.joblib"
META_PATH  = DATA_DIR / "metadata.json"

# ── Reason labels — maps lift ranges to human-readable strings ────────────────
def _reason(lift: float) -> str:
    if lift >= 1.5:  return "Frequently Bought Together"
    if lift >= 1.3:  return "Popular Combination"
    if lift >= 1.1:  return "Customers Also Bought"
    return "Recommended for You"


# ── Lazy singleton — loaded once per process ──────────────────────────────────

class _Engine:
    def __init__(self) -> None:
        self._rules: pd.DataFrame | None = None
        self._meta: dict[str, Any] = {}
        self._load_time: float = 0.0
        self._ready: bool = False

    def load(self) -> bool:
        if self._ready:
            return True
        if not RULES_PATH.exists():
            return False
        t0 = time.time()
        self._rules = joblib.load(RULES_PATH)
        self._load_time = time.time() - t0
        if META_PATH.exists():
            with open(META_PATH, encoding="utf-8") as f:
                self._meta = json.load(f)
        self._ready = True
        return True

    @property
    def is_ready(self) -> bool:
        return self._ready

    @property
    def rules(self) -> pd.DataFrame:
        return self._rules  # type: ignore[return-value]

    @property
    def meta(self) -> dict[str, Any]:
        return self._meta

    # ── Core recommendation ───────────────────────────────────────────────────

    def recommend(
        self,
        items: list[str],
        limit: int = 5,
        min_confidence: float = 0.30,
        min_lift: float = 1.0,
    ) -> list[dict[str, Any]]:
        """
        Given a list of item names, return the top `limit` recommendations.

        Algorithm:
          For each rule, check if rule.antecedents ⊆ input_items.
          If yes, the rule's consequents are candidates.
          Rank candidates by lift (primary) then confidence (secondary).
          Deduplicate: take the best rule per unique consequent item.
        """
        if not self._ready or self._rules is None or len(items) == 0:
            return []

        normalised = {i.strip().lower() for i in items}
        rules = self._rules

        # Filter: antecedents must be a subset of the input items
        mask = rules["antecedents"].apply(
            lambda ant: set(ant).issubset(normalised)
        ) & (rules["confidence"] >= min_confidence) & (rules["lift"] >= min_lift)

        matching = rules[mask].copy()
        if matching.empty:
            # Fallback: return top global rules by lift regardless of input
            matching = rules.head(limit * 3).copy()

        # Flatten: one row per consequent item (rules may have multi-item consequents)
        results: dict[str, dict[str, Any]] = {}
        for _, row in matching.iterrows():
            for item in row["consequents"]:
                if item in normalised:
                    continue  # skip items already in cart
                existing = results.get(item)
                if existing is None or row["lift"] > existing["lift"]:
                    results[item] = {
                        "recommendation": item,
                        "confidence":     round(float(row["confidence"]), 4),
                        "support":        round(float(row["support"]), 4),
                        "lift":           round(float(row["lift"]), 4),
                        "reason":         _reason(float(row["lift"])),
                    }

        # Sort by lift desc, then confidence desc
        ranked = sorted(results.values(), key=lambda x: (-x["lift"], -x["confidence"]))
        return ranked[:limit]

    def trending(self, limit: int = 5) -> list[dict[str, Any]]:
        """Top items by frequency in rule consequents (proxy for popularity)."""
        if not self._ready or self._rules is None:
            return []
        from collections import Counter
        counts: Counter = Counter()
        for items in self._rules["consequents"]:
            for item in items:
                counts[item] += 1

        # Map item popularity to support from rules
        results = []
        seen: set[str] = set()
        for row in self._rules.sort_values("lift", ascending=False).itertuples():
            for item in row.consequents:
                if item not in seen:
                    seen.add(item)
                    results.append({
                        "recommendation": item,
                        "confidence":     round(float(row.confidence), 4),
                        "support":        round(float(row.support), 4),
                        "lift":           round(float(row.lift), 4),
                        "reason":         "Trending Now",
                    })
                    if len(results) >= limit:
                        return results
        return results

    def top_rules(self, limit: int = 20) -> list[dict[str, Any]]:
        """Return top `limit` association rules by lift."""
        if not self._ready or self._rules is None:
            return []
        rows = []
        for _, r in self._rules.head(limit).iterrows():
            rows.append({
                "antecedents": list(r["antecedents"]),
                "consequents": list(r["consequents"]),
                "support":     round(float(r["support"]), 4),
                "confidence":  round(float(r["confidence"]), 4),
                "lift":        round(float(r["lift"]), 4),
                "reason":      _reason(float(r["lift"])),
            })
        return rows

    def explain(self, item: str) -> dict[str, Any]:
        """Explain why `item` is recommended (what antecedents most strongly predict it)."""
        if not self._ready or self._rules is None:
            return {"item": item, "drivers": []}

        item_lower = item.strip().lower()
        matching = self._rules[
            self._rules["consequents"].apply(lambda x: item_lower in x)
        ].sort_values("lift", ascending=False)

        drivers = []
        for _, r in matching.head(5).iterrows():
            drivers.append({
                "if_you_buy":  list(r["antecedents"]),
                "confidence":  round(float(r["confidence"]), 4),
                "lift":        round(float(r["lift"]), 4),
                "support":     round(float(r["support"]), 4),
            })
        return {
            "item":         item,
            "total_rules":  int(len(matching)),
            "drivers":      drivers,
            "explanation":  (
                f"'{item}' appears as a consequent in {len(matching)} rules. "
                f"The strongest predictor has lift={matching.head(1)['lift'].values[0]:.3f}."
                if not matching.empty else f"No rules found for '{item}'."
            ),
        }

    def analytics(self) -> dict[str, Any]:
        """Summary statistics used by the Admin AI Analytics page."""
        if not self._ready or self._rules is None:
            return {"ready": False}
        r = self._rules
        return {
            "ready":             True,
            "total_rules":       int(len(r)),
            "avg_confidence":    round(float(r["confidence"].mean()), 4),
            "avg_lift":          round(float(r["lift"].mean()), 4),
            "avg_support":       round(float(r["support"].mean()), 6),
            "max_lift":          round(float(r["lift"].max()), 4),
            "training_time_sec": self._meta.get("training_time_seconds", 0),
            "dataset":           self._meta.get("dataset", "Groceries Dataset"),
            "trained_at":        self._meta.get("trained_at", "unknown"),
            "algorithm":         self._meta.get("algorithm", "apriori"),
            "n_frequent_itemsets": self._meta.get("n_frequent_itemsets", 0),
            "top_rules":         self.top_rules(10),
            "note":              self._meta.get("note", ""),
        }


# Module-level singleton
_engine = _Engine()


def get_engine() -> _Engine:
    """Return the (possibly not-yet-loaded) engine singleton."""
    return _engine


def ensure_loaded() -> bool:
    """Load the model if not already loaded. Returns True if model is ready."""
    return _engine.load()
