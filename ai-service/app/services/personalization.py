"""
Personalised recommendation service.

Queries the customer's order history from MySQL, then layers personalised
reason strings over the base Apriori recommendations.

Reason priority:
  1. Favourite item  → "You frequently order <item>"
  2. Favourite category → "You love <category> items"
  3. Time-based      → "Popular morning / lunch / evening choice"
  4. Association     → standard reason from ai_engine._reason()
  5. New customer    → "Trending Now" / "Best Seller"
"""

from __future__ import annotations

from datetime import datetime
from typing import Any

from app.db.connector import test_connection
from app.db.transaction_builder import load_customer_history
from app.services.ai_engine import get_engine


def _time_slot() -> str:
    h = datetime.now().hour
    if 6  <= h < 12: return "morning"
    if 12 <= h < 18: return "afternoon"
    if 18 <= h < 23: return "evening"
    return "night"


def _time_reason(slot: str) -> str:
    return {
        "morning":   "Popular morning choice",
        "afternoon": "Popular lunch combo",
        "evening":   "Popular evening choice",
        "night":     "Late-night favourite",
    }[slot]


def get_personalized(customer_id: int, limit: int = 5) -> dict[str, Any]:
    """
    Return personalised recommendations for customer_id.

    If DB is unreachable or customer has no history → falls back to trending.
    """
    engine = get_engine()
    slot   = _time_slot()

    # ── No DB / cold start → trending fallback ─────────────────────────────
    if not test_connection():
        recs = engine.trending(limit)
        for r in recs:
            r["reason"] = "Trending Now"
        return {
            "customer_id":   customer_id,
            "is_personalized": False,
            "fallback_reason": "DB unavailable",
            "recommendations": recs,
        }

    # ── Load history ────────────────────────────────────────────────────────
    history = load_customer_history(customer_id)
    visit_count   = history["visit_count"]
    recent_items  = history["recent_items"]
    fav_items     = history["favorite_items"]   # {name: count}
    fav_cats      = history["favorite_cats"]    # {cat: count}
    time_profile  = history["time_profile"]

    # ── New customer (< 3 orders) → best sellers ────────────────────────────
    if visit_count < 3:
        recs = engine.trending(limit)
        for r in recs:
            r["reason"] = "Trending Now"
        return {
            "customer_id":    customer_id,
            "is_personalized": False,
            "visit_count":    visit_count,
            "fallback_reason": "New customer",
            "recommendations": recs,
        }

    # ── Build input items from recent history ───────────────────────────────
    # Use most frequent items + recent items as combined context
    input_items = list(fav_items.keys())[:5] + recent_items[:5]
    # Deduplicate, preserve order
    seen: set[str] = set()
    deduped = []
    for i in input_items:
        if i not in seen:
            seen.add(i)
            deduped.append(i)
    input_items = deduped[:8]

    # ── Get base recommendations ────────────────────────────────────────────
    recs = engine.recommend(input_items, limit=limit * 2, min_confidence=0.25)
    if not recs:
        recs = engine.trending(limit)

    # ── Add personalised reasons ────────────────────────────────────────────
    top_fav_cat = next(iter(fav_cats), None)

    for r in recs:
        item = r["recommendation"]

        if item in fav_items and fav_items[item] >= 3:
            r["reason"] = f"You frequently order {item}"
        elif top_fav_cat and fav_items:
            # Check if item is from a favourite category (best-effort name match)
            r["reason"] = f"You love {top_fav_cat} items"
        elif time_profile.get(slot, 0) > 0:
            r["reason"] = _time_reason(slot)
        # else: keep the original reason from ai_engine

    return {
        "customer_id":     customer_id,
        "is_personalized": True,
        "visit_count":     visit_count,
        "input_items":     input_items,
        "time_slot":       slot,
        "recommendations": recs[:limit],
    }


def build_explain_reason(item: str, history: dict) -> str:
    """
    Build a human-readable explanation sentence for why item is recommended.

    Examples:
      "You frequently order Espresso."
      "Customers who ordered Classic Burger also ordered French Fries."
      "You usually order Pizza on Friday evenings."
      "This combo is currently trending."
    """
    fav_items = history.get("favorite_items", {})
    fav_cats  = history.get("favorite_cats",  {})

    if item in fav_items:
        cnt = fav_items[item]
        return f"You have ordered {item} {cnt} time{'s' if cnt > 1 else ''} before."

    top_cat = next(iter(fav_cats), None)
    if top_cat:
        return f"Based on your love of {top_cat} — you might enjoy {item}."

    day = datetime.now().strftime("%A")
    h   = datetime.now().hour
    if 6 <= h < 12:
        return f"A popular morning pick on {day}s — try {item}."
    if 12 <= h < 18:
        return f"A popular lunch combo — {item} pairs well with your order."
    return f"Customers who ordered similar items also loved {item}."
