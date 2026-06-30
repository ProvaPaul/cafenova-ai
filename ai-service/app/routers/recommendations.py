"""
Recommendation endpoints — powered by Apriori (Groceries demo model).

All endpoints carry a demo_notice field so the UI can display:
    "Demo AI Recommendation — trained on public dataset"

Endpoints:
  GET  /trending        — top items regardless of input
  POST /recommend       — main endpoint: items in => recommendations out
  POST /rules           — return top association rules (for Admin)
  POST /explain         — explain why an item is recommended
  POST /personal        — legacy: personalised (maps to /recommend)
  POST /similar         — legacy: similar items (maps to /recommend)
  POST /cart            — legacy: cart-based (maps to /recommend)
  POST /pos             — legacy: POS (maps to /recommend)
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

DEMO_NOTICE = "Demo AI — trained on public Groceries dataset. Will be replaced with cafe-specific model."


def _now() -> str:
    return datetime.now(timezone.utc).isoformat()


def _engine_recs(items: list[str], limit: int, min_conf: float = 0.30, min_lift: float = 1.0) -> list[AiRecommendation]:
    engine = get_engine()
    if not engine.is_ready:
        engine.load()
    raw = engine.recommend(items, limit=limit, min_confidence=min_conf, min_lift=min_lift)
    if not raw:
        raw = engine.trending(limit)
    return [AiRecommendation(**r) for r in raw]


# ── Phase 2: Real AI endpoints ────────────────────────────────────────────────

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
    raw = engine.trending(limit)
    recs = [AiRecommendation(**r) for r in raw]
    return RecommendResponse(
        input_items=[],
        recommendations=recs,
        total=len(recs),
        generated_at=_now(),
        demo_notice=DEMO_NOTICE,
    )


@router.post(
    "/recommend",
    response_model=RecommendResponse,
    summary="Main AI endpoint — items in, recommendations out",
)
def recommend(body: RecommendRequest):
    logger.info(f"POST /recommend  items={body.items}  limit={body.limit}")
    recs = _engine_recs(body.items, body.limit, body.min_confidence, body.min_lift)
    return RecommendResponse(
        input_items=body.items,
        recommendations=recs,
        total=len(recs),
        generated_at=_now(),
        demo_notice=DEMO_NOTICE,
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
    raw = engine.top_rules(body.limit)
    rules = [RuleItem(**r) for r in raw]
    n_total = len(engine.rules) if engine.is_ready and engine.rules is not None else 0
    return RulesResponse(
        total_rules_in_model=n_total,
        showing=len(rules),
        rules=rules,
    )


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


# ── Legacy endpoints (Phase 1) remapped to real AI ────────────────────────────

@router.post(
    "/personal",
    response_model=RecommendResponse,
    summary="[Legacy] Personalised — maps to /recommend with trending fallback",
)
def get_personal(body: PersonalRecommendationRequest):
    logger.info(f"POST /personal  customer_id={body.customer_id}")
    recs = _engine_recs([], body.limit)
    return RecommendResponse(
        input_items=[],
        recommendations=recs,
        total=len(recs),
        generated_at=_now(),
        demo_notice=DEMO_NOTICE,
    )


@router.post(
    "/similar",
    response_model=RecommendResponse,
    summary="[Legacy] Similar items — maps to /recommend with product name as input",
)
def get_similar(body: SimilarItemRequest):
    items = [body.product_name] if body.product_name else []
    logger.info(f"POST /similar  product={body.product_name}")
    recs = _engine_recs(items, body.limit)
    return RecommendResponse(
        input_items=items,
        recommendations=recs,
        total=len(recs),
        generated_at=_now(),
        demo_notice=DEMO_NOTICE,
    )


@router.post(
    "/cart",
    response_model=RecommendResponse,
    summary="[Legacy] Cart-based — maps to /recommend with cart item names",
)
def get_cart(body: CartRecommendationRequest):
    logger.info(f"POST /cart  items={body.product_names}")
    recs = _engine_recs(body.product_names, body.limit)
    return RecommendResponse(
        input_items=body.product_names,
        recommendations=recs,
        total=len(recs),
        generated_at=_now(),
        demo_notice=DEMO_NOTICE,
    )


@router.post(
    "/pos",
    response_model=RecommendResponse,
    summary="[Legacy] POS You May Also Like — maps to /recommend",
)
def get_pos(body: PosRecommendationRequest):
    logger.info(f"POST /pos  items={body.current_item_names}")
    recs = _engine_recs(body.current_item_names, body.limit)
    return RecommendResponse(
        input_items=body.current_item_names,
        recommendations=recs,
        total=len(recs),
        generated_at=_now(),
        demo_notice=DEMO_NOTICE,
    )
