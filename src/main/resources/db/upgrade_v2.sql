-- ============================================================
--  Smart Cafe v2 — Schema Upgrade
--  Run once; all statements are idempotent
-- ============================================================

USE smart_cafe_db;

-- ── 1. Extend orders.status ENUM to include NEW and CONFIRMED ────────────────
--    MySQL requires re-specifying ALL values when altering an ENUM.
ALTER TABLE orders
    MODIFY COLUMN status
        ENUM('NEW','PENDING','CONFIRMED','PREPARING','IN_PROGRESS','READY','SERVED','COMPLETED','CANCELLED')
        NOT NULL DEFAULT 'NEW';

-- ── 2. Add receipt_number to payments ────────────────────────────────────────
ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS receipt_number VARCHAR(30) NULL AFTER id,
    ADD COLUMN IF NOT EXISTS payment_status ENUM('PAID','VOIDED') NOT NULL DEFAULT 'PAID' AFTER change_amount,
    ADD COLUMN IF NOT EXISTS void_reason    VARCHAR(255) NULL AFTER payment_status;

-- ── 3. Inventory movements (stock-in / stock-out / adjustment history) ───────
CREATE TABLE IF NOT EXISTS inventory_movements (
    id              INT            NOT NULL AUTO_INCREMENT,
    inventory_id    INT            NOT NULL,
    movement_type   ENUM('STOCK_IN','STOCK_OUT','ADJUSTMENT','ORDER_DEDUCTION') NOT NULL,
    quantity        DECIMAL(10,2)  NOT NULL,
    quantity_before DECIMAL(10,2)  NOT NULL,
    quantity_after  DECIMAL(10,2)  NOT NULL,
    reference_id    INT            NULL,   -- order_id for ORDER_DEDUCTION
    notes           VARCHAR(255)   NULL,
    created_by      INT            NULL,   -- user_id
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_inv_mov_item (inventory_id),
    INDEX idx_inv_mov_date (created_at),
    CONSTRAINT fk_inv_mov_item FOREIGN KEY (inventory_id)
        REFERENCES inventory_items (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── 4. In-app notifications ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS app_notifications (
    id          INT          NOT NULL AUTO_INCREMENT,
    type        ENUM('NEW_ORDER','LOW_STOCK','ORDER_COMPLETED','RESERVATION_TODAY',
                     'ORDER_STATUS','PAYMENT_VOIDED') NOT NULL,
    title       VARCHAR(150) NOT NULL,
    message     TEXT         NOT NULL,
    reference_id INT         NULL,   -- order_id / inventory_id etc.
    is_read     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_notif_read (is_read),
    INDEX idx_notif_date (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── 5. Link menu_items to inventory (optional, for auto-deduction) ───────────
ALTER TABLE menu_items
    ADD COLUMN IF NOT EXISTS inventory_id   INT  NULL AFTER is_active,
    ADD COLUMN IF NOT EXISTS stock_per_unit DECIMAL(10,3) NOT NULL DEFAULT 1.000 AFTER inventory_id;

-- ── 6. Add customer_id FK to orders (safe if already exists) ─────────────────
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS customer_id INT NULL AFTER customer_name;
