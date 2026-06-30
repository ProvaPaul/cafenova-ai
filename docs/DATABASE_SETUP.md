# Database Setup Guide

## Requirements

- MySQL 8.x running via XAMPP on **port 3307**
- Root access (or a user with CREATE DATABASE privileges)

---

## Step 1 — Create the Database

Open a terminal and run:

```bash
C:\xampp\mysql\bin\mysql.exe -u root -P 3307
```

Then in the MySQL shell:

```sql
CREATE DATABASE IF NOT EXISTS smart_cafe_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_cafe_db;
SHOW TABLES;  -- should be empty at this point
EXIT;
```

---

## Step 2 — Run the Schema Script

```bash
C:\xampp\mysql\bin\mysql.exe -u root -P 3307 smart_cafe_db < "src\main\resources\db\schema.sql"
```

This creates all tables, indexes, and inserts seed data (categories, cafe tables).

**Tables created:**

| Table | Purpose |
|---|---|
| `users` | Staff accounts with roles |
| `password_reset_tokens` | Secure one-time reset tokens |
| `categories` | Menu categories |
| `menu_items` | Products/menu items |
| `cafe_tables` | Physical seating tables |
| `orders` | Customer orders |
| `order_items` | Line items per order |
| `payments` | Payment records |
| `inventory_items` | Ingredient/supply stock |
| `suppliers` | Supplier directory |
| `ai_suggestions` | AI-generated insights (future) |
| `customers` | Cafe guest CRM |
| `employees` | Staff HR records |
| `attendance` | Daily attendance log |
| `salary_payments` | Monthly payroll records |
| `reservations` | Table reservations |

---

## Step 3 — Insert Default Users

The schema does not insert users (passwords are BCrypt hashed — they cannot be set with a plain SQL INSERT).

Run this one-time setup script in MySQL to create the four default accounts:

```sql
USE smart_cafe_db;

-- Admin account (password: admin123)
INSERT INTO users (full_name, username, email, password_hash, role) VALUES
('System Admin', 'admin', 'admin@smartcafe.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhn/', 'ADMIN');

-- Manager account (password: manager123)
INSERT INTO users (full_name, username, email, password_hash, role) VALUES
('Cafe Manager', 'manager', 'manager@smartcafe.com',
 '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'MANAGER');

-- Cashier account (password: cashier123)
INSERT INTO users (full_name, username, email, password_hash, role) VALUES
('Front Cashier', 'cashier', 'cashier@smartcafe.com',
 '$2a$10$KIVtVSCAl6WT8JYdELFQ1.vG1NcEBEu9sGj6.ZwFvS3VXCdODW2kq', 'CASHIER');

-- Kitchen account (password: kitchen123)
INSERT INTO users (full_name, username, email, password_hash, role) VALUES
('Kitchen Staff', 'kitchen', 'kitchen@smartcafe.com',
 '$2a$10$e0NR3YP7b.L7eKQz4y3Cqew0TluRJ4Vy9rL5R9bRRG5J8bEVwwjO.', 'KITCHEN_STAFF');
```

> **Note:** These BCrypt hashes were generated from the passwords above. They will work with the
> BCrypt verification in `AuthServiceImpl`. Do NOT modify the `password_hash` values.

---

## Step 4 — Verify the Setup

```bash
C:\xampp\mysql\bin\mysql.exe -u root -P 3307 smart_cafe_db
```

```sql
SHOW TABLES;
SELECT id, username, role, is_active FROM users;
SELECT id, name FROM categories;
SELECT id, table_number, capacity, location FROM cafe_tables;
```

Expected output: 16 tables, 4 users, 6 categories, 9 tables.

---

## Backup and Restore

The application includes a built-in Backup/Restore function under **Settings → Database**.

**Manual backup via terminal:**
```bash
C:\xampp\mysql\bin\mysqldump.exe -u root -P 3307 smart_cafe_db > backup.sql
```

**Manual restore:**
```bash
C:\xampp\mysql\bin\mysql.exe -u root -P 3307 smart_cafe_db < backup.sql
```

---

## Re-running the Schema (Fresh Start)

To wipe and recreate:

```sql
DROP DATABASE IF EXISTS smart_cafe_db;
CREATE DATABASE smart_cafe_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Then repeat Steps 2 and 3.

---

## Database Configuration File

`src/main/resources/config/database.properties`

```properties
db.url=jdbc:mysql://localhost:3307/smart_cafe_db?useSSL=false&serverTimezone=Asia/Manila
db.username=root
db.password=
db.pool.maximumPoolSize=10
db.pool.minimumIdle=2
db.pool.connectionTimeout=30000
db.pool.idleTimeout=600000
db.pool.maxLifetime=1800000
```

The connection pool (HikariCP) is shut down cleanly via a JVM shutdown hook in `Main.java`.
