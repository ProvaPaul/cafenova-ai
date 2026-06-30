-- Smart Cafe Website — Incremental Schema
-- Safe to run on every startup (all statements are idempotent)

-- Add auth columns to the shared customers table
ALTER TABLE customers
    ADD COLUMN IF NOT EXISTS username VARCHAR(50) NULL UNIQUE,
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT FALSE;

-- Add unique index on email (customers coming from website must have unique email)
ALTER TABLE customers
    ADD UNIQUE IF NOT EXISTS uq_customer_email (email);

-- Make cashier_id nullable so website orders don't require a staff user
ALTER TABLE orders MODIFY COLUMN cashier_id INT NULL;

-- Cart items (persistent per customer)
CREATE TABLE IF NOT EXISTS customer_cart_items (
    id           INT NOT NULL AUTO_INCREMENT,
    customer_id  INT NOT NULL,
    menu_item_id INT NOT NULL,
    quantity     INT NOT NULL DEFAULT 1,
    added_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_cart_cust_item (customer_id, menu_item_id),
    CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id)  REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_menu     FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Product reviews / feedback
CREATE TABLE IF NOT EXISTS customer_feedback (
    id           INT NOT NULL AUTO_INCREMENT,
    customer_id  INT NULL,
    menu_item_id INT NULL,
    order_id     INT NULL,
    rating       TINYINT NOT NULL DEFAULT 5,
    review       TEXT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_feedback_item (menu_item_id),
    CONSTRAINT fk_feedback_customer FOREIGN KEY (customer_id)  REFERENCES customers(id) ON DELETE SET NULL,
    CONSTRAINT fk_feedback_item     FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE SET NULL,
    CONSTRAINT fk_feedback_order    FOREIGN KEY (order_id)     REFERENCES orders(id)    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Discount coupons
CREATE TABLE IF NOT EXISTS coupons (
    id           INT NOT NULL AUTO_INCREMENT,
    code         VARCHAR(20) NOT NULL,
    description  VARCHAR(255) NULL,
    discount_pct DECIMAL(5,2) NULL,
    discount_amt DECIMAL(10,2) NULL,
    min_order    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    max_uses     INT NOT NULL DEFAULT 100,
    used_count   INT NOT NULL DEFAULT 0,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at   TIMESTAMP NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_coupon_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Customer ↔ Coupon assignment
CREATE TABLE IF NOT EXISTS customer_coupons (
    id          INT NOT NULL AUTO_INCREMENT,
    customer_id INT NOT NULL,
    coupon_id   INT NOT NULL,
    is_used     BOOLEAN NOT NULL DEFAULT FALSE,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at     TIMESTAMP NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_cust_coupon (customer_id, coupon_id),
    CONSTRAINT fk_cc_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_cc_coupon   FOREIGN KEY (coupon_id)   REFERENCES coupons(id)   ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- In-app notifications for customers
CREATE TABLE IF NOT EXISTS customer_notifications (
    id          INT NOT NULL AUTO_INCREMENT,
    customer_id INT NOT NULL,
    title       VARCHAR(200) NOT NULL,
    message     TEXT NOT NULL,
    type        ENUM('ORDER','RESERVATION','LOYALTY','PROMO') NOT NULL DEFAULT 'ORDER',
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_notif_customer (customer_id),
    CONSTRAINT fk_notif_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample coupon for testing
INSERT IGNORE INTO coupons (code, description, discount_pct, min_order, max_uses)
VALUES ('WELCOME10', '10% off your first order', 10.00, 100.00, 1000);
