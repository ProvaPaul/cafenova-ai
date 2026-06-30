"""
Admin router — AI management endpoints for the Admin Dashboard.

POST   /api/v1/admin/generate-demo   — insert demo data
POST   /api/v1/admin/retrain         — retrain Apriori from DB
DELETE /api/v1/admin/demo-data       — remove all demo data
GET    /api/v1/admin/training-history — list training sessions
GET    /api/v1/admin/ai-status        — AI health + model version
GET    /api/v1/admin/export-report    — full AI report JSON
"""

from __future__ import annotations

import json
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime
from typing import Any

from fastapi import APIRouter, HTTPException

from app.db.connector import get_db, test_connection
from app.services.ai_engine import get_engine, ensure_loaded
from app.services.model_registry import list_model_files, get_active_metadata

router = APIRouter(tags=["Admin"])

# Background executor for long-running tasks (training can take 5–30s)
_executor = ThreadPoolExecutor(max_workers=1, thread_name_prefix="ai_admin")


# ── Helper: run blocking function in thread ───────────────────────────────────
import asyncio


async def _run(fn, *args):
    loop = asyncio.get_event_loop()
    return await loop.run_in_executor(_executor, fn, *args)


# ── POST /generate-demo ───────────────────────────────────────────────────────

@router.post("/admin/generate-demo")
async def generate_demo():
    """Insert 200 demo customers, 100 menu items, and ~2 500 COMPLETED orders."""
    from scripts.generate_demo_data import generate
    result = await _run(generate)
    if not result.get("success"):
        raise HTTPException(status_code=400, detail=result.get("message", "Failed"))
    return result


# ── POST /retrain ─────────────────────────────────────────────────────────────

@router.post("/admin/retrain")
async def retrain():
    """
    Retrain Apriori from MySQL, save versioned model, and hot-reload the engine.
    """
    from scripts.train_from_db import train
    result = await _run(train)

    if not result.get("success"):
        raise HTTPException(status_code=400, detail=result.get("message", "Training failed"))

    # Hot-reload the engine so new model is used immediately
    engine = get_engine()
    engine._ready = False          # reset flag
    engine._rules = None
    engine._meta  = {}
    await _run(ensure_loaded)

    result["engine_reloaded"] = engine.is_ready
    return result


# ── DELETE /demo-data ─────────────────────────────────────────────────────────

@router.delete("/admin/demo-data")
async def delete_demo():
    """Remove all demo customers, orders, menu items, and categories."""
    from scripts.generate_demo_data import delete_demo as _del
    result = await _run(_del)
    if not result.get("success"):
        raise HTTPException(status_code=400, detail=result.get("message", "Failed"))
    return result


# ── GET /training-history ─────────────────────────────────────────────────────

@router.get("/admin/training-history")
def training_history(limit: int = 20) -> dict[str, Any]:
    """Return the last N training sessions from ai_training_sessions."""
    if not test_connection():
        return {"connected": False, "sessions": []}

    try:
        with get_db() as conn:
            cur = conn.cursor(dictionary=True)
            cur.execute(
                """SELECT id, version, model_file, dataset_source,
                          n_transactions, n_unique_items, n_rules,
                          avg_confidence, avg_lift, training_time_sec,
                          status, trained_at
                   FROM   ai_training_sessions
                   ORDER  BY id DESC
                   LIMIT  %s""",
                (limit,),
            )
            rows = cur.fetchall()
            cur.close()

        # Convert datetime to ISO string for JSON serialisation
        for r in rows:
            if isinstance(r.get("trained_at"), datetime):
                r["trained_at"] = r["trained_at"].isoformat()

        return {"connected": True, "sessions": rows, "total": len(rows)}
    except Exception as e:
        return {"connected": False, "sessions": [], "error": str(e)}


# ── GET /ai-status ────────────────────────────────────────────────────────────

@router.get("/admin/ai-status")
def ai_status() -> dict[str, Any]:
    """Health check + active model version."""
    engine   = get_engine()
    meta     = get_active_metadata()
    models   = list_model_files()
    db_ok    = test_connection()

    return {
        "engine_ready":     engine.is_ready,
        "total_rules":      len(engine.rules) if engine.is_ready and engine.rules is not None else 0,
        "active_version":   meta.get("version", "none"),
        "dataset_source":   meta.get("dataset_source", "unknown"),
        "trained_at":       meta.get("trained_at", "never"),
        "avg_confidence":   meta.get("avg_confidence", 0.0),
        "avg_lift":         meta.get("avg_lift", 0.0),
        "n_versions":       len(models),
        "model_files":      models[:5],
        "db_connected":     db_ok,
    }


# ── GET /export-report ────────────────────────────────────────────────────────

@router.get("/admin/export-report")
def export_report() -> dict[str, Any]:
    """Full AI report — suitable for download or display."""
    engine = get_engine()
    meta   = get_active_metadata()

    history_resp = training_history(50)
    status_resp  = ai_status()

    top_rules = []
    if engine.is_ready and engine.rules is not None:
        top_rules = engine.top_rules(20)

    return {
        "report_generated_at": datetime.utcnow().isoformat(),
        "system":              "SmartCafe AI Recommendation Engine",
        "version":             meta.get("version", "unknown"),
        "status":              status_resp,
        "active_model":        meta,
        "top_rules":           top_rules,
        "training_history":    history_resp.get("sessions", []),
        "model_files":         list_model_files(),
    }
