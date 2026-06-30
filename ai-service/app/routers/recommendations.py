"""
Recommendation endpoints — Apriori-based, trained on Cafe database.

Endpoints:
  GET  /trending              — top items (no input, public)
  POST /recommend             — items in => recommendations out
  POST /rules                 — top association rules (Admin)
  POST /explain               — explain why an item is recommended
  GET  /personal/{customer_id}— personalised from order history (NEW)
  POST /personal              — legacy POST (Spring Boot backward-compat)
  POST /similar               — legacy similar items
  POST /cart                  — legacy cart-based
  POST /pos                   — legacy POS
"""

from datetime import datetime, timezone

from fastapi import APIRouter, Query
from loguru import logger

from app.models.request import (
    RecommendRequest,
    RulesRequest,
    ExplainRequest,
    PersonalRecommendationRequest,
    SimilarItemRequest,
    CartRecommendationRequest,
    PosRecommendationRequest,
)
from app.models.response import (
    AiRecommendation,
    RecommendResponse,
    RulesResponse,
    RuleItem,
    ExplainResponse,
    RecommendationResponse,
    RecommendedItem,
)
from app.services.ai_engine import get_engine

router = APIRouter(prefix="/recommendations", tags=["Recommendations"])


def _now() -> str:
    return datetime.now(timezone.utc).isoformat()


def _source_and_notice() -> tuple[str, str]:
    """Return (source_id, demo_notice) based on the active model metadata."""
    engine = get_engine()
    if not engine.is_ready:
        return "none", "AI model not loaded."
    src = engine.meta.get("dataset_source", "GROCERIES_CSV")
    ver = engine.meta.get("version", "apriori_v1")
    if src == "CAFE_ORDERS":
        return ver, ""
    if src == "DEMO_DATA":
        return ver, "Trained on demo dataset. Retrain with real orders for production."
    return "apriori_groceries_v1", (
        "Demo AI — trained on public Groceries dataset. "
        "Generate demo data and retrain to use cafe-specific recommendations."
    )


def _engine_recs(
    items: list[str],
    limit: int,
    min_conf: float = 0.25,
    min_lift: float = 1.0,
) -> list[AiRecommendation]:
    engine = get_engine()
    if not engine.is_ready:
        engine.load()
    raw = engine.recommend(items, limit=limit, min_confidence=min_conf, min_lift=min_lift)
    if not raw:
        raw = engine.trending(limit)
    return [AiRecommendation(**r) for r in raw]


# ── Real AI endpoints ─────────────────────────────────────────────────────────

@router.get(
    "/trending",
    response_model=RecommendResponse,
    summary="Top recommended items (no input needed)",
)
def get_trending(limit: int = Query(default=5, ge=1, le=20)):
    logger.info(f"GET /trending  limit={limit}")
    engine = get_engine()
    if not engine.is_ready:
        engine.load()
    raw  = engine.trending(limit)
    recs = [AiRecommendation(**r) for r in raw]
    src, notice = _source_and_notice()
    return RecommendResponse(
        source=src,
        input_items=[],
        recommendations=recs,
        total=len(recs),
        generated_at=_now(),
        demo_notice=notice,
    )


@router.post(
    "/recommend",
    response_model=RecommendResponse,
    summary="Main AI endpoint — items in, recommendations out",
)
def recommend(body: RecommendRequest):
    logger.info(f"POST /recommend  items={body.items}  limit={body.limit}")
    recs = _engine_recs(body.items, body.limit, body.min_confidence, body.min_lift)
    src, notice = _source_and_notice()
    return RecommendResponse(
        source=src,
        input_items=body.items,
        recommendations=recs,
        total=len(recs),
        generated_at=_now(),
        demo_notice=notice,
    )


@router.post(
    "/rules",
    response_model=RulesResponse,
    summary="Return top association rules (for Admin AI Analytics)",
)
def get_rules(body: RulesRequest = RulesRequest()):
    logger.info(f"POST /rules  limit={body.limit}")
    engine = get_engine()
    if not engine.is_ready:
        engine.load()
    raw   = engine.top_rules(body.limit)
    rules = [RuleItem(**r) for r in raw]
    n_total = len(engine.rules) if engine.is_ready and engine.rules is not None else 0
    return RulesResponse(total_rules_in_model=n_total, showing=len(rules), rules=rules)


@router.post(
    "/explain",
    response_model=ExplainResponse,
    summary="Explain why an item is recommended",
)
def explain(body: ExplainRequest):
    logger.info(f"POST /explain  item={body.item}")
    engine = get_engine()
    if not engine.is_ready:
        engine.load()
    data = engine.explain(body.item)
    return ExplainResponse(**data)


@router.get(
    "/personal/{customer_id}",
    response_model=RecommendResponse,
    summary="Personalised recommendations from customer order history",
)
def get_personal_by_id(customer_id: int, limit: int = Query(default=5, ge=1, le=20)):
    """
    Uses the customer's order history from MySQL to personalise recommendations.
    Falls back to trending if customer has < 3 orders or DB is unreachable.
    """
    logger.info(f"GET /personal/{customer_id}  limit={limit}")
    from app.services.personalization import get_personalized
    data = get_personalized(customer_id, limit)
    raw  = data.get("recommendations", [])
    recs = [AiRecommendation(**r) for r in raw if isinstance(r, dict)]
    src, notice = _source_and_notice()
    return RecommendResponse(
        source=src,
        input_items=data.get("input_items", []),
        recommendations=recs,
        total=len(recs),
        generated_at=_now(),
        demo_notice=notice,
        extra={
            "is_personalized":  data.get("is_personalized", False),
            "visit_count":      data.get("visit_count", 0),
            "time_slot":        data.get("time_slot", ""),
            "fallback_reason":  data.get("fallback_reason", ""),
        },
    )


# ── Legacy endpoints (backward-compat with Spring Boot) ──────────────────────

@router.post("/personal", response_model=RecommendResponse, include_in_schema=False)
def get_personal(body: PersonalRecommendationRequest):
    """Legacy POST /personal — delegates to GET /personal/{customer_id} if id given."""
    if body.customer_id:
        return get_personal_by_id(body.customer_id, body.limit)
    recs = _engine_recs([], body.limit)
    src, notice = _source_and_notice()
    return RecommendResponse(
        source=src, input_items=[], recommendations=recs,
        total=len(recs), generated_at=_now(), demo_notice=notice,
    )


@router.post("/similar", response_model=RecommendResponse, include_in_schema=False)
def get_similar(body: SimilarItemRequest):
    items = [body.product_name] if body.product_name else []
    recs  = _engine_recs(items, body.limit)
    src, notice = _source_and_notice()
    return RecommendResponse(
        source=src, input_items=items, recommendations=recs,
        total=len(recs), generated_at=_now(), demo_notice=notice,
    )


@router.post("/cart", response_model=RecommendResponse, include_in_schema=False)
def get_cart(body: CartRecommendationRequest):
    recs = _engine_recs(body.product_names, body.limit)
    src, notice = _source_and_notice()
    return RecommendResponse(
        source=src, input_items=body.product_names, recommendations=recs,
        total=len(recs), generated_at=_now(), demo_notice=notice,
    )


@router.post("/pos", response_model=RecommendResponse, include_in_schema=False)
def get_pos(body: PosRecommendationRequest):
    recs = _engine_recs(body.current_item_names, body.limit)
    src, notice = _source_and_notice()
    return RecommendResponse(
        source=src, input_items=body.current_item_names, recommendations=recs,
        total=len(recs), generated_at=_now(), demo_notice=notice,
    )
