from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from loguru import logger

from app.config import get_settings
from app.models.response import HealthResponse
from app.routers import recommendations, analytics
from app.routers import admin as admin_router
from app.services.ai_engine import ensure_loaded, get_engine
from app.utils.logger import setup_logging


# ── Startup / shutdown ─────────────────────────────────────────────────────────
@asynccontextmanager
async def lifespan(app: FastAPI):
    settings = get_settings()
    setup_logging(debug=settings.debug)
    logger.info(f"{settings.app_name} v{settings.app_version} starting up")
    logger.info(f"Listening on http://{settings.app_host}:{settings.app_port}")

    # Load trained model (cafe DB model if available, Groceries otherwise)
    ok = ensure_loaded()
    engine = get_engine()
    if ok:
        n     = len(engine.rules) if engine.rules is not None else 0
        src   = engine.meta.get("dataset_source", "unknown")
        ver   = engine.meta.get("version", "v?")
        logger.info(f"AI model loaded: {n} rules  version={ver}  source={src}")
    else:
        logger.warning("AI model not found — use Admin panel to generate demo data and retrain")

    # Scheduled auto-retrain (runs daily at AUTO_RETRAIN_HOUR if non-zero)
    scheduler = None
    if settings.auto_retrain_hour > 0:
        try:
            from apscheduler.schedulers.asyncio import AsyncIOScheduler
            from scripts.train_from_db import train as _train

            async def _scheduled_retrain():
                logger.info("Scheduled retrain starting...")
                import asyncio
                from concurrent.futures import ThreadPoolExecutor
                ex = ThreadPoolExecutor(max_workers=1)
                loop = asyncio.get_event_loop()
                result = await loop.run_in_executor(ex, _train)
                if result.get("success"):
                    engine._ready = False
                    engine._rules = None
                    engine._meta  = {}
                    ensure_loaded()
                    logger.info(f"Scheduled retrain complete: {result.get('n_rules')} rules")
                else:
                    logger.warning(f"Scheduled retrain failed: {result.get('message')}")

            scheduler = AsyncIOScheduler()
            scheduler.add_job(
                _scheduled_retrain,
                "cron",
                hour=settings.auto_retrain_hour,
                minute=0,
            )
            scheduler.start()
            logger.info(f"Auto-retrain scheduled at {settings.auto_retrain_hour:02d}:00 daily")
        except ImportError:
            logger.info("APScheduler not installed — auto-retrain disabled")

    yield

    if scheduler and scheduler.running:
        scheduler.shutdown(wait=False)
    logger.info("Shutting down AI service")


settings = get_settings()

app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    description=(
        "AI Recommendation & Analytics API for Smart Cafe Management System.\n\n"
        "**Phase 3**: Real Apriori model trained on the Cafe's own `orders` and "
        "`order_items` tables in MySQL.\n\n"
        "Use the **Admin** section to generate demo data, retrain, and export reports."
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

# ── Global error handler ──────────────────────────────────────────────────────
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
app.include_router(admin_router.router,    prefix="/api/v1")


# ── Health / root ─────────────────────────────────────────────────────────────
@app.get("/", include_in_schema=False)
def root():
    return {"service": settings.app_name, "version": settings.app_version, "docs": "/docs"}


@app.get("/health", response_model=HealthResponse, tags=["Health"])
def health():
    engine  = get_engine()
    n_rules = len(engine.rules) if engine.is_ready and engine.rules is not None else 0
    src     = engine.meta.get("dataset_source", "unknown") if engine.is_ready else "none"
    ver     = engine.meta.get("version", "—") if engine.is_ready else "—"
    return HealthResponse(
        status="ok",
        service=settings.app_name,
        version=settings.app_version,
        ai_ready=engine.is_ready,
        total_rules=n_rules,
        message=(
            f"Model loaded: {n_rules} rules  version={ver}  source={src}"
            if engine.is_ready
            else "Model not loaded. Use Admin panel to generate demo data and retrain."
        ),
    )


@app.get("/api/v1/health", response_model=HealthResponse, tags=["Health"])
def health_v1():
    return health()
