"""
MySQL connection factory for the AI service.

Uses mysql-connector-python (pure Python, no C extension required).
All queries run in synchronous helpers; FastAPI calls them in a thread executor.
"""

from __future__ import annotations

from contextlib import contextmanager
from typing import Generator

import mysql.connector
from mysql.connector import MySQLConnection, Error as MySQLError

from app.config import get_settings


def _make_connection() -> MySQLConnection:
    s = get_settings()
    return mysql.connector.connect(
        host=s.db_host,
        port=s.db_port,
        user=s.db_user,
        password=s.db_password,
        database=s.db_name,
        charset="utf8mb4",
        autocommit=False,
        connection_timeout=10,
        use_pure=True,
    )


@contextmanager
def get_db() -> Generator[MySQLConnection, None, None]:
    """Context manager: yields a connected session, commits on success, rolls back on error."""
    conn = _make_connection()
    try:
        yield conn
        conn.commit()
    except MySQLError:
        conn.rollback()
        raise
    finally:
        conn.close()


def test_connection() -> bool:
    """Return True if the DB is reachable, False otherwise."""
    try:
        with get_db() as conn:
            cur = conn.cursor()
            cur.execute("SELECT 1")
            cur.fetchone()
            cur.close()
        return True
    except Exception:
        return False


def get_cashier_ids() -> list[int]:
    """Return all active user IDs usable as cashier_id for demo orders."""
    try:
        with get_db() as conn:
            cur = conn.cursor()
            cur.execute("SELECT id FROM users WHERE is_active = TRUE LIMIT 10")
            rows = cur.fetchall()
            cur.close()
        return [r[0] for r in rows] or [1]
    except Exception:
        return [1]
