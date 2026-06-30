"""
Model registry — tracks all trained versions and exposes the active model path.

Layout on disk:
    data/
      models/
        cafe_v20260630_155000_rules.joblib
        cafe_v20260630_162300_rules.joblib
      trained_rules.joblib   ← always the latest (copy, not symlink)
      metadata.json          ← metadata for the active model
"""

from __future__ import annotations

import json
from datetime import datetime
from pathlib import Path
from typing import Any

DATA_DIR   = Path(__file__).parent.parent.parent / "data"
MODELS_DIR = DATA_DIR / "models"
RULES_PATH = DATA_DIR / "trained_rules.joblib"
META_PATH  = DATA_DIR / "metadata.json"


def list_model_files() -> list[dict[str, Any]]:
    """Return all versioned model files, newest first."""
    MODELS_DIR.mkdir(parents=True, exist_ok=True)
    files = sorted(MODELS_DIR.glob("*_rules.joblib"), reverse=True)
    return [
        {
            "version":   f.stem.replace("_rules", ""),
            "file":      f.name,
            "size_kb":   round(f.stat().st_size / 1024, 1),
            "created_at": datetime.fromtimestamp(f.stat().st_mtime).isoformat(),
        }
        for f in files
    ]


def get_active_metadata() -> dict[str, Any]:
    """Load and return the active model's metadata.json."""
    if not META_PATH.exists():
        return {}
    with open(META_PATH, encoding="utf-8") as f:
        return json.load(f)


def model_exists() -> bool:
    return RULES_PATH.exists()
