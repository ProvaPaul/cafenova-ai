"""
Builds the Apriori transaction list from the MySQL database.

Pipeline:
    orders (COMPLETED)
        + order_items
        + menu_items
    → list of lists  [["Espresso","Brownie"], ["Classic Burger","French Fries","Coke"], ...]

Each completed order is one transaction.  The transaction contains the unique
menu item names bought in that order (quantity is ignored for association rules).
"""

from __future__ import annotations

from app.db.connector import get_db


def load_transactions() -> tuple[list[list[str]], dict]:
    """
    Fetch all COMPLETED order transactions from the DB.

    Returns:
        transactions: list[list[str]]   — one list per order, item names
        stats: dict                     — n_orders, n_items, n_unique_items
    """
    sql = """
        SELECT
            o.id                AS order_id,
            mi.name             AS item_name
        FROM   orders       o
        JOIN   order_items  oi ON oi.order_id     = o.id
        JOIN   menu_items   mi ON mi.id           = oi.menu_item_id
        WHERE  o.status = 'COMPLETED'
        ORDER  BY o.id, mi.name
    """
    with get_db() as conn:
        cur = conn.cursor()
        cur.execute(sql)
        rows = cur.fetchall()
        cur.close()

    # Group by order_id
    basket: dict[int, set[str]] = {}
    for order_id, item_name in rows:
        basket.setdefault(order_id, set()).add(item_name.strip())

    transactions = [sorted(items) for items in basket.values() if len(items) >= 2]

    all_items = {item for t in transactions for item in t}
    stats = {
        "n_transactions": len(transactions),
        "n_unique_items":  len(all_items),
        "n_raw_rows":      len(rows),
    }
    return transactions, stats


def load_customer_history(customer_id: int) -> dict:
    """
    Fetch a customer's purchase history for personalisation.

    Returns a dict with:
        recent_items      — last 15 items ordered (for recommendation input)
        favorite_items    — top 10 items by order count
        favorite_cats     — top 5 categories by order count
        visit_count       — total number of completed orders
        total_spent       — total spend amount
        time_profile      — {"morning": N, "afternoon": M, "evening": K}
    """
    with get_db() as conn:
        cur = conn.cursor()

        # Recent items (last 15 distinct item names from last 10 orders)
        cur.execute("""
            SELECT DISTINCT mi.name
            FROM   orders       o
            JOIN   order_items  oi ON oi.order_id = o.id
            JOIN   menu_items   mi ON mi.id = oi.menu_item_id
            WHERE  o.customer_id = %s AND o.status = 'COMPLETED'
            ORDER  BY o.created_at DESC, mi.name
            LIMIT  15
        """, (customer_id,))
        recent_items = [r[0] for r in cur.fetchall()]

        # Favorite items
        cur.execute("""
            SELECT mi.name, COUNT(*) AS cnt
            FROM   orders       o
            JOIN   order_items  oi ON oi.order_id = o.id
            JOIN   menu_items   mi ON mi.id = oi.menu_item_id
            WHERE  o.customer_id = %s AND o.status = 'COMPLETED'
            GROUP  BY mi.name
            ORDER  BY cnt DESC
            LIMIT  10
        """, (customer_id,))
        fav_items = {r[0]: r[1] for r in cur.fetchall()}

        # Favorite categories
        cur.execute("""
            SELECT c.name, COUNT(*) AS cnt
            FROM   orders       o
            JOIN   order_items  oi ON oi.order_id = o.id
            JOIN   menu_items   mi ON mi.id = oi.menu_item_id
            JOIN   categories    c ON c.id  = mi.category_id
            WHERE  o.customer_id = %s AND o.status = 'COMPLETED'
            GROUP  BY c.name
            ORDER  BY cnt DESC
            LIMIT  5
        """, (customer_id,))
        fav_cats = {r[0]: r[1] for r in cur.fetchall()}

        # Visit count & total spent
        cur.execute("""
            SELECT COUNT(*), COALESCE(SUM(total), 0)
            FROM   orders
            WHERE  customer_id = %s AND status = 'COMPLETED'
        """, (customer_id,))
        row = cur.fetchone()
        visit_count = int(row[0]) if row else 0
        total_spent = float(row[1]) if row else 0.0

        # Time profile
        cur.execute("""
            SELECT
                SUM(HOUR(created_at) BETWEEN  6 AND 11) AS morning,
                SUM(HOUR(created_at) BETWEEN 12 AND 17) AS afternoon,
                SUM(HOUR(created_at) BETWEEN 18 AND 22) AS evening
            FROM orders
            WHERE customer_id = %s AND status = 'COMPLETED'
        """, (customer_id,))
        tp = cur.fetchone()
        time_profile = {
            "morning":   int(tp[0] or 0) if tp else 0,
            "afternoon": int(tp[1] or 0) if tp else 0,
            "evening":   int(tp[2] or 0) if tp else 0,
        }

        cur.close()

    return {
        "recent_items":   recent_items,
        "favorite_items": fav_items,
        "favorite_cats":  fav_cats,
        "visit_count":    visit_count,
        "total_spent":    total_spent,
        "time_profile":   time_profile,
    }
