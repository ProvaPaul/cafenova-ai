-- ============================================================
--  Smart AI-Based Cafe Management System — Database Schema
--  Engine: MySQL 8.x
--  Charset: utf8mb4 (full Unicode + emoji support)
-- ============================================================

CREATE DATABASE IF NOT EXISTS smart_cafe_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_cafe_db;

-- ─── AUTHENTICATION & USERS ──────────────────────────────────────────────────

-- Staff accounts. Role is stored as ENUM so the column is self-documenting and
-- constrainted without a foreign key join on every login query.
CREATE TABLE IF NOT EXISTS users (
    id            INT            NOT NULL AUTO_INCREMENT,
    full_name     VARCHAR(100)   NOT NULL,
    username      VARCHAR(50)    NOT NULL,
    email         VARCHAR(100)   NOT NULL,
    password_hash VARCHAR(255)   NOT NULL,        -- BCrypt hash (never plaintext)
    phone         VARCHAR(20)    NULL,
    role          ENUM('ADMIN','MANAGER','CASHIER','KITCHEN_STAFF') NOT NULL,
    is_active     BOOLEAN        NOT NULL DEFAULT TRUE,
    created_by    INT            NULL,             -- FK to users.id (who created this account)
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_users_username (username),
    UNIQUE KEY uq_users_email    (email),
    CONSTRAINT fk_users_created_by FOREIGN KEY (created_by) REFERENCES users (id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Secure password-reset tokens. One token per request, single-use, 1-hour TTL.
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id         INT          NOT NULL AUTO_INCREMENT,
    user_id    INT          NOT NULL,
    token      VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    is_used    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_prt_token (token),
    INDEX idx_prt_user (user_id),
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ─── MENU ────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS categories (
    id          INT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    description TEXT         NULL,
    sort_order  INT          NOT NULL DEFAULT 0,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_cat_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS menu_items (
    id           INT            NOT NULL AUTO_INCREMENT,
    category_id  INT            NOT NULL,
    name         VARCHAR(100)   NOT NULL,
    description  TEXT           NULL,
    price        DECIMAL(10, 2) NOT NULL,
    cost_price   DECIMAL(10, 2) NULL,       -- used for profit margin calculations
    image_path   VARCHAR(255)   NULL,
    is_available BOOLEAN        NOT NULL DEFAULT TRUE,
    is_active    BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_menu_category (category_id),
    CONSTRAINT fk_menu_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ─── TABLES (physical seating) ───────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS cafe_tables (
    id           INT         NOT NULL AUTO_INCREMENT,
    table_number VARCHAR(10) NOT NULL,
    capacity     INT         NOT NULL DEFAULT 4,
    location     VARCHAR(50) NULL,       -- e.g. 'Indoor', 'Outdoor', 'VIP'
    status       ENUM('AVAILABLE','OCCUPIED','RESERVED','MAINTENANCE')
                             NOT NULL DEFAULT 'AVAILABLE',
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_table_number (table_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ─── ORDERS ──────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS orders (
    id           INT            NOT NULL AUTO_INCREMENT,
    order_number VARCHAR(20)    NOT NULL,          -- e.g. "ORD-20260629-0001"
    table_id     INT            NULL,              -- NULL for takeaway/delivery
    cashier_id   INT            NOT NULL,
    order_type   ENUM('DINE_IN','TAKEAWAY','DELIVERY') NOT NULL DEFAULT 'DINE_IN',
    status       ENUM('PENDING','IN_PROGRESS','READY','SERVED','COMPLETED','CANCELLED')
                               NOT NULL DEFAULT 'PENDING',
    subtotal     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax          DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total        DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    notes        TEXT           NULL,
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_order_number (order_number),
    INDEX idx_order_table    (table_id),
    INDEX idx_order_cashier  (cashier_id),
    INDEX idx_order_status   (status),
    INDEX idx_order_date     (created_at),
    CONSTRAINT fk_order_table   FOREIGN KEY (table_id)   REFERENCES cafe_tables (id),
    CONSTRAINT fk_order_cashier FOREIGN KEY (cashier_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS order_items (
    id           INT            NOT NULL AUTO_INCREMENT,
    order_id     INT            NOT NULL,
    menu_item_id INT            NOT NULL,
    quantity     INT            NOT NULL DEFAULT 1,
    unit_price   DECIMAL(10,2) NOT NULL,
    subtotal     DECIMAL(10,2) NOT NULL,
    notes        TEXT           NULL,
    status       ENUM('PENDING','IN_PROGRESS','READY','SERVED') NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_oi_order (order_id),
    INDEX idx_oi_item  (menu_item_id),
    CONSTRAINT fk_oi_order FOREIGN KEY (order_id)     REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_oi_item  FOREIGN KEY (menu_item_id) REFERENCES menu_items (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ─── PAYMENTS ────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS payments (
    id               INT            NOT NULL AUTO_INCREMENT,
    order_id         INT            NOT NULL,
    payment_method   ENUM('CASH','CARD','MOBILE_PAYMENT') NOT NULL,
    amount_paid      DECIMAL(10,2) NOT NULL,
    change_amount    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    transaction_ref  VARCHAR(100)   NULL,       -- card/mobile reference number
    cashier_id       INT            NOT NULL,
    paid_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_payment_order (order_id),    -- one payment record per order
    INDEX idx_payment_cashier (cashier_id),
    CONSTRAINT fk_payment_order   FOREIGN KEY (order_id)   REFERENCES orders (id),
    CONSTRAINT fk_payment_cashier FOREIGN KEY (cashier_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ─── INVENTORY ───────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS inventory_items (
    id             INT            NOT NULL AUTO_INCREMENT,
    name           VARCHAR(100)   NOT NULL,
    unit           VARCHAR(20)    NOT NULL,    -- e.g. 'kg', 'litre', 'piece'
    current_stock  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    min_stock      DECIMAL(10,2) NOT NULL DEFAULT 0.00,   -- triggers low-stock alert
    cost_per_unit  DECIMAL(10,2) NULL,
    supplier_id    INT            NULL,
    is_active      BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_inventory_name (name),
    INDEX idx_inventory_supplier (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS suppliers (
    id         INT          NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    contact    VARCHAR(100) NULL,
    phone      VARCHAR(20)  NULL,
    email      VARCHAR(100) NULL,
    address    TEXT         NULL,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_supplier_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE inventory_items
    ADD CONSTRAINT fk_inventory_supplier
    FOREIGN KEY (supplier_id) REFERENCES suppliers (id) ON DELETE SET NULL;


-- ─── AI / ANALYTICS ──────────────────────────────────────────────────────────
-- Stores AI-generated suggestions (demand forecasts, restock alerts, etc.)
-- The AI module will populate this table; the dashboard reads from it.

CREATE TABLE IF NOT EXISTS ai_suggestions (
    id              INT          NOT NULL AUTO_INCREMENT,
    suggestion_type ENUM('RESTOCK','DEMAND_FORECAST','MENU_RECOMMENDATION',
                         'REVENUE_INSIGHT','STAFFING') NOT NULL,
    title           VARCHAR(200) NOT NULL,
    body            TEXT         NOT NULL,
    confidence      DECIMAL(5,2) NULL,     -- 0-100 confidence score
    is_read         BOOLEAN      NOT NULL DEFAULT FALSE,
    generated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_ai_type (suggestion_type),
    INDEX idx_ai_date (generated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ─── SEED DATA ───────────────────────────────────────────────────────────────
-- Default categories to get started quickly

INSERT IGNORE INTO categories (name, description, sort_order) VALUES
    ('Hot Beverages',  'Coffee, tea, and other hot drinks', 1),
    ('Cold Beverages', 'Iced drinks, smoothies, juices',    2),
    ('Pastries',       'Croissants, muffins, cakes',        3),
    ('Main Courses',   'Sandwiches, wraps, rice dishes',    4),
    ('Desserts',       'Ice cream, cheesecake, waffles',    5),
    ('Snacks',         'Light bites and side orders',       6);

-- Default cafe tables
INSERT IGNORE INTO cafe_tables (table_number, capacity, location) VALUES
    ('T01', 2, 'Indoor'), ('T02', 2, 'Indoor'), ('T03', 4, 'Indoor'),
    ('T04', 4, 'Indoor'), ('T05', 6, 'Indoor'), ('T06', 6, 'Indoor'),
    ('T07', 4, 'Outdoor'),('T08', 4, 'Outdoor'),('V01', 8, 'VIP');
