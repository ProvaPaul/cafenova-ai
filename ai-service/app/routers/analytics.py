"""
Analytics endpoints — mix of real AI data and business stubs.

  GET /analytics/ai         — REAL: trained model stats (rules, conf, lift, etc.)
  GET /analytics/dashboard  — stub: business KPI dashboard
  GET /analytics/sales-trends  — stub: sales forecast
  GET /analytics/peak-hours    — stub: peak hour analysis
  GET /analytics/customer-segments — stub: customer segmentation
"""

from fastapi import APIRouter, Query
from loguru import logger

from app.models.response import (
    AiAnalyticsResponse,
    RuleItem,
    AnalyticsDashboardResponse,
    AnalyticsMetric,
    SalesTrendResponse,
    SalesTrendPoint,
    PeakHoursResponse,
    PeakHourSlot,
    CustomerSegmentsResponse,
    CustomerSegment,
)
from app.services.ai_engine import get_engine
from app.services.analytics_service import (
    get_dashboard_analytics,
    get_sales_trends,
    get_peak_hours,
    get_customer_segments,
)

router = APIRouter(prefix="/analytics", tags=["Analytics"])


@router.get(
    "/ai",
    response_model=AiAnalyticsResponse,
    summary="Real AI model statistics — rules, confidence, lift, training time",
)
def get_ai_analytics():
    logger.info("GET /analytics/ai")
    engine = get_engine()
    if not engine.is_ready:
        engine.load()
    data = engine.analytics()
    if not data.get("ready"):
        return AiAnalyticsResponse(
            ready=False,
            dataset="Model not loaded. Run preprocess.py then train.py first.",
        )
    top = [RuleItem(**r) for r in data.get("top_rules", [])]
    return AiAnalyticsResponse(
        ready=True,
        total_rules=data["total_rules"],
        avg_confidence=data["avg_confidence"],
        avg_lift=data["avg_lift"],
        avg_support=data["avg_support"],
        max_lift=data["max_lift"],
        training_time_sec=data["training_time_sec"],
        dataset=data["dataset"],
        algorithm=data["algorithm"],
        trained_at=data["trained_at"],
        n_frequent_itemsets=data["n_frequent_itemsets"],
        top_rules=top,
        note=data["note"],
    )


@router.get(
    "/dashboard",
    response_model=AnalyticsDashboardResponse,
    summary="Business KPI dashboard metrics and AI insights",
)
def get_dashboard():
    logger.info("GET /analytics/dashboard")
    return get_dashboard_analytics()


@router.get(
    "/sales-trends",
    response_model=SalesTrendResponse,
    summary="Historical sales trend + 7-day forecast",
)
def get_sales_trends_route(days: int = Query(default=14, ge=7, le=90)):
    logger.info(f"GET /analytics/sales-trends  days={days}")
    return get_sales_trends(days=days)


@router.get(
    "/peak-hours",
    response_model=PeakHoursResponse,
    summary="Hourly order distribution and peak hour analysis",
)
def get_peak_hours_route():
    logger.info("GET /analytics/peak-hours")
    return get_peak_hours()


@router.get(
    "/customer-segments",
    response_model=CustomerSegmentsResponse,
    summary="Customer segmentation (High-Value, Loyal, At-Risk, Churned)",
)
def get_customer_segments_route():
    logger.info("GET /analytics/customer-segments")
    return get_customer_segments()
