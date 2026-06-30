-- ============================================================
--  Smart Cafe v3 — Schema Upgrade
--  AI Training Sessions table + indexes for personalization
--  Run once; all statements are idempotent
-- ============================================================

USE smart_cafe_db;

-- ── AI Training Session Log ───────────────────────────────────────────────────
-- Tracks every retrain run: source, parameters, results, timing.
-- Populated by ai-service/scripts/train_from_db.py
-- Read by Admin AI Analytics panel via GET /api/v1/admin/training-history

CREATE TABLE IF NOT EXISTS ai_training_sessions (
    id                  INT            NOT NULL AUTO_INCREMENT,
    version             VARCHAR(30)    NOT NULL,                  -- e.g. "cafe_v3"
    model_file          VARCHAR(255)   NOT NULL,                  -- relative path under ai-service/data/
    dataset_source      ENUM('CAFE_ORDERS','DEMO_DATA','GROCERIES_CSV') NOT NULL DEFAULT 'CAFE_ORDERS',
    n_transactions      INT            NOT NULL DEFAULT 0,
    n_unique_items      INT            NOT NULL DEFAULT 0,
    n_frequent_itemsets INT            NOT NULL DEFAULT 0,
    n_rules             INT            NOT NULL DEFAULT 0,
    min_support         FLOAT          NOT NULL DEFAULT 0.02,
    min_confidence      FLOAT          NOT NULL DEFAULT 0.30,
    avg_confidence      FLOAT          NOT NULL DEFAULT 0.0,
    avg_lift            FLOAT          NOT NULL DEFAULT 0.0,
    max_lift            FLOAT          NOT NULL DEFAULT 0.0,
    training_time_sec   FLOAT          NOT NULL DEFAULT 0.0,
    status              ENUM('RUNNING','COMPLETED','FAILED') NOT NULL DEFAULT 'RUNNING',
    error_message       TEXT           NULL,
    notes               TEXT           NULL,
    trained_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_ai_ts_status (status),
    INDEX idx_ai_ts_date   (trained_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ── Indexes to speed up AI personalization queries ────────────────────────────
-- Personalization queries join orders + order_items + menu_items by customer_id.
-- These indexes make GET /api/v1/recommendations/personal fast even at 100k orders.

CREATE INDEX IF NOT EXISTS idx_orders_customer
    ON orders (customer_id);

CREATE INDEX IF NOT EXISTS idx_orders_status_customer
    ON orders (status, customer_id);

CREATE INDEX IF NOT EXISTS idx_orders_created
    ON orders (created_at);
