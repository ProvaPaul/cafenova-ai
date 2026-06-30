from pydantic import BaseModel, Field


# ── Phase 2: Real AI endpoints ────────────────────────────────────────────────

class RecommendRequest(BaseModel):
    items: list[str] = Field(default_factory=list, description="Items in cart / being viewed")
    limit: int = Field(default=5, ge=1, le=20)
    min_confidence: float = Field(default=0.30, ge=0.0, le=1.0)
    min_lift: float = Field(default=1.0, ge=0.0)


class RulesRequest(BaseModel):
    limit: int = Field(default=20, ge=1, le=100)


class ExplainRequest(BaseModel):
    item: str = Field(..., description="Item to explain")


# ── Phase 1 legacy request models (kept for Spring Boot backward-compat) ──────

class PersonalRecommendationRequest(BaseModel):
    customer_id: int | None = None
    limit: int = Field(default=5, ge=1, le=20)
    context: str | None = None


class SimilarItemRequest(BaseModel):
    product_id: int
    product_name: str | None = None
    category: str | None = None
    limit: int = Field(default=5, ge=1, le=20)


class CartRecommendationRequest(BaseModel):
    product_ids: list[int] = Field(default_factory=list)
    product_names: list[str] = Field(default_factory=list)
    limit: int = Field(default=5, ge=1, le=20)


class PosRecommendationRequest(BaseModel):
    cashier_id: int | None = None
    current_item_ids: list[int] = Field(default_factory=list)
    current_item_names: list[str] = Field(default_factory=list)
    table_id: int | None = None
    limit: int = Field(default=4, ge=1, le=10)


class AnalyticsRequest(BaseModel):
    from_date: str | None = None
    to_date: str | None = None
    limit: int = Field(default=10, ge=1, le=100)
