from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from loguru import logger

from app.config import get_settings
from app.models.response import HealthResponse
from app.routers import recommendations, analytics
from app.services.ai_engine import ensure_loaded, get_engine
from app.utils.logger import setup_logging


@asynccontextmanager
async def lifespan(app: FastAPI):
    settings = get_settings()
    setup_logging(debug=settings.debug)
    logger.info(f"{settings.app_name} v{settings.app_version} starting up")
    logger.info(f"Listening on http://{settings.app_host}:{settings.app_port}")

    # Load the trained model at startup so first request is fast
    ok = ensure_loaded()
    engine = get_engine()
    if ok:
        n = len(engine.rules) if engine.rules is not None else 0
        logger.info(f"AI model loaded: {n} association rules ready")
    else:
        logger.warning("AI model not found — run preprocess.py then train.py")

    yield
    logger.info("Shutting down AI service")


settings = get_settings()

app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    description=(
        "AI Recommendation & Analytics API for Smart Cafe Management System.\n\n"
        "**Phase 2**: Running a real Apriori model trained on the public "
        "[Groceries dataset](https://www.kaggle.com/datasets/heeraldedhia/groceries-dataset) "
        "for validation purposes.\n\n"
        "This model will later be replaced by one trained on the cafe's own "
        "`orders` and `order_items` tables. The API interface (endpoints, DTOs) "
        "will remain identical.\n\n"
        "**Demo Notice**: All `/recommendations/*` responses include a `demo_notice` field."
    ),
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan,
)

# ── CORS ─────────────────────────────────────────────────────────────────────
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.allowed_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── Global exception handler ──────────────────────────────────────────────────
@app.exception_handler(Exception)
async def unhandled_exception(request: Request, exc: Exception):
    logger.exception(f"Unhandled error on {request.method} {request.url}: {exc}")
    return JSONResponse(
        status_code=500,
        content={"success": False, "error": "Internal server error", "detail": str(exc)},
    )

# ── Routers ───────────────────────────────────────────────────────────────────
app.include_router(recommendations.router, prefix="/api/v1")
app.include_router(analytics.router,       prefix="/api/v1")


# ── Health / root ─────────────────────────────────────────────────────────────
@app.get("/", include_in_schema=False)
def root():
    return {"service": settings.app_name, "version": settings.app_version, "docs": "/docs"}


@app.get("/health", response_model=HealthResponse, tags=["Health"])
def health():
    engine = get_engine()
    n_rules = len(engine.rules) if engine.is_ready and engine.rules is not None else 0
    return HealthResponse(
        status="ok",
        service=settings.app_name,
        version=settings.app_version,
        ai_ready=engine.is_ready,
        total_rules=n_rules,
        message=(
            f"Apriori model loaded: {n_rules} rules (Groceries demo)."
            if engine.is_ready
            else "Model not loaded. Run preprocess.py then train.py."
        ),
    )


@app.get("/api/v1/health", response_model=HealthResponse, tags=["Health"])
def health_v1():
    return health()
