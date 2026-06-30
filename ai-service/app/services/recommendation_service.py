"""
Recommendation Service — dummy engine that returns realistic cafe recommendations.

Architecture note:
  This module is the placeholder for the real ML engine.
  When the actual model is ready, replace the _dummy_* methods with
  real collaborative filtering / content-based / association-rule calls.
  The public interface stays the same so callers don't change.
"""

import random
from datetime import datetime, timezone

from app.models.response import RecommendedItem, RecommendationResponse


# ── Master catalog of cafe items used in recommendations ─────────────────────
# These map to real menu_items inserted by seed_data.sql
_CATALOG: list[dict] = [
    {"id": 35, "name": "Caramel Macchiato",      "category": "Espresso Drinks",     "price": 145.00},
    {"id": 38, "name": "Mocha Frappuccino",       "category": "Frappuccinos",         "price": 165.00},
    {"id": 43, "name": "Brown Sugar Bubble Tea",  "category": "Tea & Infusions",      "price": 145.00},
    {"id": 67, "name": "Belgian Waffle Classic",  "category": "Waffles & Pancakes",   "price": 165.00},
    {"id": 22, "name": "Tiramisu",                "category": "Desserts",             "price": 155.00},
    {"id": 11, "name": "Butter Croissant",        "category": "Pastries",             "price": 85.00},
    {"id": 26, "name": "Truffle Fries",           "category": "Snacks",               "price": 145.00},
    {"id": 21, "name": "New York Cheesecake",     "category": "Desserts",             "price": 145.00},
    {"id": 44, "name": "Matcha Latte",            "category": "Tea & Infusions",      "price": 145.00},
    {"id": 51, "name": "Orange Juice",            "category": "Fresh Juices",         "price": 105.00},
    {"id": 16, "name": "Club Sandwich",           "category": "Main Courses",         "price": 195.00},
    {"id": 33, "name": "Cappuccino",              "category": "Espresso Drinks",      "price": 125.00},
    {"id": 7,  "name": "Taro Milk Tea",           "category": "Cold Beverages",       "price": 130.00},
    {"id": 12, "name": "Blueberry Muffin",        "category": "Pastries",             "price": 90.00},
    {"id": 23, "name": "Chocolate Lava Cake",     "category": "Desserts",             "price": 165.00},
    {"id": 56, "name": "Classic Chocolate Shake", "category": "Milkshakes",           "price": 165.00},
    {"id": 71, "name": "Eggs Benedict",           "category": "Breakfast Items",      "price": 265.00},
    {"id": 61, "name": "Margherita Flatbread",    "category": "Pizza & Flatbreads",   "price": 195.00},
    {"id": 81, "name": "Triple Chocolate Cake",   "category": "Cakes & Slices",       "price": 165.00},
    {"id": 39, "name": "Caramel Ribbon Crunch",   "category": "Frappuccinos",         "price": 175.00},
    {"id": 31, "name": "Flat White",              "category": "Espresso Drinks",      "price": 130.00},
    {"id": 68, "name": "Ube Waffle",              "category": "Waffles & Pancakes",   "price": 185.00},
    {"id": 24, "name": "Leche Flan",              "category": "Desserts",             "price": 120.00},
    {"id": 57, "name": "Salted Caramel Shake",    "category": "Milkshakes",           "price": 175.00},
    {"id": 78, "name": "Mango Cheesecake",        "category": "Cakes & Slices",       "price": 155.00},
]

# ── Reason / algorithm pools ──────────────────────────────────────────────────
_TRENDING_REASONS = [
    ("Trending Now",          "popularity"),
    ("Top Seller Today",      "popularity"),
    ("Most Ordered This Week","popularity"),
]

_FBT_REASONS = [
    ("Frequently Bought Together", "association_rules"),
    ("Popular Pairing",            "association_rules"),
    ("Customers Also Bought",      "association_rules"),
    ("Goes Well With",             "content_based"),
]

_PERSONAL_REASONS = [
    ("Based on Your Orders",    "collaborative_filtering"),
    ("You Might Like",          "collaborative_filtering"),
    ("Matches Your Taste",      "collaborative_filtering"),
    ("Recommended for You",     "collaborative_filtering"),
]

_CART_REASONS = [
    ("Complements Your Cart",   "association_rules"),
    ("Popular Add-on",          "association_rules"),
    ("Don't Forget This",       "content_based"),
    ("Pairs Well With",         "content_based"),
]

_POS_REASONS = [
    ("You May Also Like",       "collaborative_filtering"),
    ("Popular Upsell",          "popularity"),
    ("Customers Added This",    "association_rules"),
    ("Suggested Add-on",        "content_based"),
]


def _now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


def _build_items(
    pool: list[dict],
    reason_pool: list[tuple[str, str]],
    limit: int,
    exclude_ids: list[int] | None = None,
    confidence_base: float = 0.78,
    confidence_spread: float = 0.18,
) -> list[RecommendedItem]:
    exclude = set(exclude_ids or [])
    candidates = [c for c in pool if c["id"] not in exclude]
    # Shuffle deterministically by id so results feel stable but varied per call
    rng = random.Random(sum(exclude) + len(candidates))
    rng.shuffle(candidates)
    candidates = candidates[:limit]

    items = []
    for i, item in enumerate(candidates):
        reason, algo = reason_pool[i % len(reason_pool)]
        confidence = round(max(0.70, confidence_base + confidence_spread * (1 - i / max(limit, 1))), 2)
        items.append(RecommendedItem(
            id=item["id"],
            name=item["name"],
            category=item["category"],
            price=item["price"],
            reason=reason,
            confidence=confidence,
            algorithm=algo,
        ))
    return items


# ── Public service methods ────────────────────────────────────────────────────

def get_trending(limit: int = 5) -> RecommendationResponse:
    items = _build_items(
        _CATALOG, _TRENDING_REASONS, limit,
        confidence_base=0.85, confidence_spread=0.12,
    )
    return RecommendationResponse(
        context="trending",
        recommendations=items,
        total=len(items),
        generated_at=_now_iso(),
    )


def get_personal(customer_id: int | None, limit: int = 5, context: str = "home") -> RecommendationResponse:
    # Use customer_id as RNG seed so each customer gets consistent personal picks
    rng_seed = customer_id or 42
    pool = random.Random(rng_seed).sample(_CATALOG, min(len(_CATALOG), limit + 5))
    items = _build_items(
        pool, _PERSONAL_REASONS, limit,
        confidence_base=0.82, confidence_spread=0.14,
    )
    return RecommendationResponse(
        context=f"personal:{context}",
        recommendations=items,
        total=len(items),
        generated_at=_now_iso(),
    )


def get_similar(product_id: int, product_name: str | None, category: str | None, limit: int = 5) -> RecommendationResponse:
    # Prioritise same category, then others
    same_cat = [c for c in _CATALOG if c.get("category") == category and c["id"] != product_id]
    other = [c for c in _CATALOG if c.get("category") != category and c["id"] != product_id]
    pool = (same_cat + other)[:limit + 4]
    items = _build_items(
        pool, _FBT_REASONS, limit,
        exclude_ids=[product_id],
        confidence_base=0.80, confidence_spread=0.15,
    )
    return RecommendationResponse(
        context=f"similar:{product_id}",
        recommendations=items,
        total=len(items),
        generated_at=_now_iso(),
    )


def get_cart_recommendations(product_ids: list[int], product_names: list[str], limit: int = 5) -> RecommendationResponse:
    items = _build_items(
        _CATALOG, _CART_REASONS, limit,
        exclude_ids=product_ids,
        confidence_base=0.79, confidence_spread=0.16,
    )
    return RecommendationResponse(
        context="cart",
        recommendations=items,
        total=len(items),
        generated_at=_now_iso(),
    )


def get_pos_recommendations(
    current_item_ids: list[int],
    current_item_names: list[str],
    limit: int = 4,
) -> RecommendationResponse:
    items = _build_items(
        _CATALOG, _POS_REASONS, limit,
        exclude_ids=current_item_ids,
        confidence_base=0.81, confidence_spread=0.15,
    )
    return RecommendationResponse(
        context="pos",
        recommendations=items,
        total=len(items),
        generated_at=_now_iso(),
    )
