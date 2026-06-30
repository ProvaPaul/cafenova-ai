from pydantic import BaseModel
from typing import Any


# ── Phase 2: Real AI response models ─────────────────────────────────────────

class AiRecommendation(BaseModel):
    """Single recommendation — matches the required format exactly."""
    recommendation: str
    confidence: float
    support: float
    lift: float
    reason: str


class RecommendResponse(BaseModel):
    success: bool = True
    source: str = "apriori_groceries_v1"
    demo_notice: str = ""
    input_items: list[str] = []
    recommendations: list[AiRecommendation]
    total: int
    generated_at: str
    extra: dict[str, Any] = {}


class RuleItem(BaseModel):
    antecedents: list[str]
    consequents: list[str]
    support: float
    confidence: float
    lift: float
    reason: str


class RulesResponse(BaseModel):
    success: bool = True
    total_rules_in_model: int
    showing: int
    rules: list[RuleItem]


class ExplainResponse(BaseModel):
    success: bool = True
    item: str
    total_rules: int
    drivers: list[dict[str, Any]]
    explanation: str


class AiAnalyticsResponse(BaseModel):
    success: bool = True
    ready: bool
    total_rules: int = 0
    avg_confidence: float = 0.0
    avg_lift: float = 0.0
    avg_support: float = 0.0
    max_lift: float = 0.0
    training_time_sec: float = 0.0
    dataset: str = ""
    algorithm: str = ""
    trained_at: str = ""
    n_frequent_itemsets: int = 0
    top_rules: list[RuleItem] = []
    note: str = ""


# ── Phase 1 legacy models (still used by Spring Boot proxy) ──────────────────

class RecommendedItem(BaseModel):
    """Legacy model — kept for Spring Boot backward-compat."""
    id: int = 0
    name: str
    category: str = "General"
    price: float = 0.0
    image_url: str | None = None
    reason: str
    confidence: float
    algorithm: str = "apriori"


class RecommendationResponse(BaseModel):
    success: bool = True
    context: str
    recommendations: list[RecommendedItem]
    total: int
    generated_at: str


# ── Analytics stub models ─────────────────────────────────────────────────────

class AnalyticsMetric(BaseModel):
    label: str
    value: Any
    change: float | None = None
    trend: str | None = None


class AnalyticsDashboardResponse(BaseModel):
    success: bool = True
    period: str
    metrics: list[AnalyticsMetric]
    insights: list[str]
    generated_at: str


class SalesTrendPoint(BaseModel):
    date: str
    revenue: float
    order_count: int
    predicted: bool = False


class SalesTrendResponse(BaseModel):
    success: bool = True
    trend: list[SalesTrendPoint]
    summary: str


class PeakHourSlot(BaseModel):
    hour: str
    order_count: int
    revenue: float
    is_peak: bool


class PeakHoursResponse(BaseModel):
    success: bool = True
    peak_hours: list[PeakHourSlot]
    busiest_hour: str
    recommendation: str


class CustomerSegment(BaseModel):
    segment: str
    count: int
    description: str
    suggested_action: str
    avg_spend: float


class CustomerSegmentsResponse(BaseModel):
    success: bool = True
    segments: list[CustomerSegment]
    total_customers: int


class ApiError(BaseModel):
    success: bool = False
    error: str
    detail: str | None = None


class HealthResponse(BaseModel):
    status: str
    service: str
    version: str
    ai_ready: bool
    total_rules: int = 0
    message: str
