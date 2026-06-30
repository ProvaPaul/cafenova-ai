"""
Generate Realistic Demo Data for SmartCafe AI Training.

Inserts into the live MySQL database:
  - 10 new categories  (Coffee, Tea, Burger, Pizza, Pasta, Sandwich,
                         Dessert, Juice, Soft Drinks, Snacks)
  - 100 menu items     (10 per category)
  - 200 customers
  - ~2 500 COMPLETED orders with 2-6 items each
  - Realistic buying patterns: Burger+Fries+Coke, Coffee+Brownie, etc.

Demo data markers (used for safe deletion later):
  - Orders:    order_number starts with 'DMO-'
  - Customers: email ends with '@demo.smartcafe.local'

Saves data/demo_metadata.json so admin router can delete cleanly.

Run standalone:
    cd ai-service
    python -m scripts.generate_demo_data

Or called from FastAPI endpoint:
    from scripts.generate_demo_data import generate
    result = generate()
"""

from __future__ import annotations

import json
import random
import sys
from datetime import datetime, timedelta
from pathlib import Path

# ── Allow running from repo root as   python -m scripts.generate_demo_data ───
sys.path.insert(0, str(Path(__file__).parent.parent))

import mysql.connector as _mc

from app.db.connector import get_cashier_ids, get_db
from app.config import get_settings as _get_settings


def _raw_conn(autocommit: bool = False):
    """Open a raw connection with buffered results (avoids 'Unread result' errors)."""
    s = _get_settings()
    return _mc.connect(
        host=s.db_host, port=s.db_port,
        user=s.db_user, password=s.db_password,
        database=s.db_name, charset="utf8mb4",
        autocommit=autocommit,
    )

DATA_DIR = Path(__file__).parent.parent / "data"
META_FILE = DATA_DIR / "demo_metadata.json"


# ── Categories ────────────────────────────────────────────────────────────────
DEMO_CATEGORIES = [
    ("Coffee",      "Espresso drinks, lattes, cold brews and more",        30),
    ("Tea",         "Green, black, herbal, bubble and milk teas",          31),
    ("Burger",      "Classic, chicken and gourmet burgers",                32),
    ("Pizza",       "Personal-size and sharing pizzas",                    33),
    ("Pasta",       "Spaghetti, carbonara, lasagna and more",              34),
    ("Sandwich",    "Club, wraps, melts and gourmet sandwiches",           35),
    ("Dessert",     "Cakes, pastries, puddings and tarts",                 36),
    ("Juice",       "Fresh-pressed and blended juices",                    37),
    ("Soft Drinks", "Colas, sodas and bottled beverages",                  38),
    ("Snacks",      "Fries, wings, rings and bite-size snacks",            39),
]

# ── Products: (name, price, cost, image_url) ─────────────────────────────────
_U = "https://images.unsplash.com/photo-"
_P = "?w=80&h=80&fit=crop&fm=jpg&q=70"

DEMO_PRODUCTS: dict[str, list[tuple[str, float, float, str]]] = {
    "Coffee": [
        ("Espresso",       90.00,  35.00, _U + "1510707577719-ae7c14805e3a" + _P),
        ("Americano",      95.00,  38.00, _U + "1461023058943-07fcbe16d735" + _P),
        ("Latte",         120.00,  48.00, _U + "1534787198879-9781e34b5ad4" + _P),
        ("Cappuccino",    115.00,  45.00, _U + "1572490122747-3968b75cc699" + _P),
        ("Flat White",    125.00,  50.00, _U + "1509042239860-b5e95c7c4db4" + _P),
        ("Mocha",         130.00,  52.00, _U + "1578862100389-bc1e734d5b51" + _P),
        ("Caramel Latte", 135.00,  55.00, _U + "1526045612212-70cfa2e4a5fb" + _P),
        ("Iced Coffee",   110.00,  44.00, _U + "1559496417-e7f25cb247a3"   + _P),
        ("Cold Brew",     130.00,  52.00, _U + "1545665277-25ff24b4cb8a"   + _P),
        ("Macchiato",     100.00,  40.00, _U + "1504630083234-14187a70cb35" + _P),
    ],
    "Tea": [
        ("Jasmine Tea",    80.00, 28.00, _U + "1556679862-9373f8a54c49"   + _P),
        ("Green Tea",      85.00, 30.00, _U + "1612929633738-8fe44f7ec841" + _P),
        ("Chamomile Tea",  85.00, 30.00, _U + "1576092768241-dec231879fc3" + _P),
        ("Earl Grey",      90.00, 32.00, _U + "1544787219-7f47ccb76574"   + _P),
        ("Iced Tea",       75.00, 25.00, _U + "1558618047-4f86e5a7f7d4"   + _P),
        ("Milk Tea",       95.00, 35.00, _U + "1558618047-4f86e5a7f7d4"   + _P),
        ("Matcha Latte",  120.00, 48.00, _U + "1568649929195-dc7b4e5e8afe" + _P),
        ("Taro Milk Tea", 115.00, 45.00, _U + "1558618047-4f86e5a7f7d4"   + _P),
        ("Thai Iced Tea", 100.00, 38.00, _U + "1556679862-9373f8a54c49"   + _P),
        ("Lemon Tea",      80.00, 28.00, _U + "1576092768241-dec231879fc3" + _P),
    ],
    "Burger": [
        ("Classic Burger",        195.00,  80.00, _U + "1568901346375-a96444dc1f0e" + _P),
        ("Cheeseburger",          215.00,  88.00, _U + "1586190848861-65591ef25bc7" + _P),
        ("Chicken Burger",        185.00,  76.00, _U + "1550317138-10000687a72b"    + _P),
        ("Double Patty Burger",   265.00, 110.00, _U + "1568901346375-a96444dc1f0e" + _P),
        ("BBQ Burger",            225.00,  92.00, _U + "1586190848861-65591ef25bc7" + _P),
        ("Mushroom Burger",       205.00,  84.00, _U + "1550317138-10000687a72b"    + _P),
        ("Veggie Burger",         185.00,  75.00, _U + "1568901346375-a96444dc1f0e" + _P),
        ("Crispy Chicken Burger", 195.00,  80.00, _U + "1586190848861-65591ef25bc7" + _P),
        ("Bacon Burger",          245.00, 100.00, _U + "1550317138-10000687a72b"    + _P),
        ("Spicy Burger",          215.00,  88.00, _U + "1568901346375-a96444dc1f0e" + _P),
    ],
    "Pizza": [
        ("Margherita Pizza",       255.00, 100.00, _U + "1565299624946-b28f40a0ae38" + _P),
        ("Pepperoni Pizza",        285.00, 112.00, _U + "1513104890138-cf55e4ea8b36" + _P),
        ("BBQ Chicken Pizza",      295.00, 116.00, _U + "1565299624946-b28f40a0ae38" + _P),
        ("Hawaiian Pizza",         275.00, 108.00, _U + "1513104890138-cf55e4ea8b36" + _P),
        ("Veggie Supreme Pizza",   265.00, 104.00, _U + "1565299624946-b28f40a0ae38" + _P),
        ("Four Cheese Pizza",      295.00, 116.00, _U + "1513104890138-cf55e4ea8b36" + _P),
        ("Meat Lovers Pizza",      315.00, 124.00, _U + "1565299624946-b28f40a0ae38" + _P),
        ("Garlic Bread Pizza",     245.00,  96.00, _U + "1513104890138-cf55e4ea8b36" + _P),
        ("Buffalo Chicken Pizza",  295.00, 116.00, _U + "1565299624946-b28f40a0ae38" + _P),
        ("Italian Sausage Pizza",  285.00, 112.00, _U + "1513104890138-cf55e4ea8b36" + _P),
    ],
    "Pasta": [
        ("Spaghetti Bolognese", 195.00,  80.00, _U + "1551892374516-6fe7576f5e2f" + _P),
        ("Carbonara",           205.00,  84.00, _U + "1548943487-a2e838e7d78e"    + _P),
        ("Pesto Pasta",         185.00,  76.00, _U + "1551892374516-6fe7576f5e2f" + _P),
        ("Alfredo Pasta",       215.00,  88.00, _U + "1548943487-a2e838e7d78e"    + _P),
        ("Arrabbiata",          185.00,  76.00, _U + "1551892374516-6fe7576f5e2f" + _P),
        ("Mac and Cheese",      175.00,  72.00, _U + "1548943487-a2e838e7d78e"    + _P),
        ("Lasagna",             225.00,  92.00, _U + "1551892374516-6fe7576f5e2f" + _P),
        ("Seafood Pasta",       245.00, 100.00, _U + "1548943487-a2e838e7d78e"    + _P),
        ("Aglio e Olio",        185.00,  76.00, _U + "1551892374516-6fe7576f5e2f" + _P),
        ("Mushroom Pasta",      195.00,  80.00, _U + "1548943487-a2e838e7d78e"    + _P),
    ],
    "Sandwich": [
        ("Club Sandwich",       175.00,  72.00, _U + "1528735602780-e3e7bde9b0a6" + _P),
        ("BLT Sandwich",        165.00,  68.00, _U + "1619740455090-5dad9f7c1e5e" + _P),
        ("Tuna Melt",           155.00,  64.00, _U + "1528735602780-e3e7bde9b0a6" + _P),
        ("Grilled Cheese",      145.00,  60.00, _U + "1619740455090-5dad9f7c1e5e" + _P),
        ("Turkey Sandwich",     175.00,  72.00, _U + "1528735602780-e3e7bde9b0a6" + _P),
        ("Chicken Caesar Wrap", 185.00,  76.00, _U + "1619740455090-5dad9f7c1e5e" + _P),
        ("Veggie Wrap",         165.00,  68.00, _U + "1528735602780-e3e7bde9b0a6" + _P),
        ("Egg Salad Sandwich",  145.00,  60.00, _U + "1619740455090-5dad9f7c1e5e" + _P),
        ("Philly Cheesesteak",  195.00,  80.00, _U + "1528735602780-e3e7bde9b0a6" + _P),
        ("Monte Cristo",        185.00,  76.00, _U + "1619740455090-5dad9f7c1e5e" + _P),
    ],
    "Dessert": [
        ("Chocolate Cake",   135.00, 54.00, _U + "1571877153374-486968965701" + _P),
        ("Cheesecake",       145.00, 58.00, _U + "1588195538326-c5b1e9f80a1b" + _P),
        ("Brownie",           95.00, 38.00, _U + "1606313564200-9484e285aab7" + _P),
        ("Tiramisu",         150.00, 60.00, _U + "1571877153374-486968965701" + _P),
        ("Carrot Cake",      130.00, 52.00, _U + "1588195538326-c5b1e9f80a1b" + _P),
        ("Lemon Tart",       140.00, 56.00, _U + "1606313564200-9484e285aab7" + _P),
        ("Red Velvet Cake",  135.00, 54.00, _U + "1571877153374-486968965701" + _P),
        ("Macarons",         110.00, 44.00, _U + "1588195538326-c5b1e9f80a1b" + _P),
        ("Chocolate Pudding",120.00, 48.00, _U + "1606313564200-9484e285aab7" + _P),
        ("Cream Puff",       105.00, 42.00, _U + "1571877153374-486968965701" + _P),
    ],
    "Juice": [
        ("Orange Juice",       95.00, 35.00, _U + "1600271886132-bd2d3b9b7a72" + _P),
        ("Apple Juice",        90.00, 33.00, _U + "1543362906-acfc16c67564"    + _P),
        ("Mango Juice",        95.00, 35.00, _U + "1600271886132-bd2d3b9b7a72" + _P),
        ("Pineapple Juice",    90.00, 33.00, _U + "1543362906-acfc16c67564"    + _P),
        ("Watermelon Juice",   85.00, 31.00, _U + "1600271886132-bd2d3b9b7a72" + _P),
        ("Mixed Berry Juice", 105.00, 38.00, _U + "1543362906-acfc16c67564"    + _P),
        ("Lemonade",           85.00, 31.00, _U + "1563227812-0ea4b2de3cde"    + _P),
        ("Grapefruit Juice",   95.00, 35.00, _U + "1600271886132-bd2d3b9b7a72" + _P),
        ("Pomegranate Juice", 105.00, 38.00, _U + "1543362906-acfc16c67564"    + _P),
        ("Carrot Juice",       90.00, 33.00, _U + "1600271886132-bd2d3b9b7a72" + _P),
    ],
    "Soft Drinks": [
        ("Coke",            65.00, 22.00, _U + "1554786407-0e9d3e01dd42"    + _P),
        ("Sprite",          65.00, 22.00, _U + "1585238342024-8d4e7ed6c47b" + _P),
        ("Pepsi",           65.00, 22.00, _U + "1554786407-0e9d3e01dd42"    + _P),
        ("Mountain Dew",    65.00, 22.00, _U + "1585238342024-8d4e7ed6c47b" + _P),
        ("Root Beer",       70.00, 24.00, _U + "1554786407-0e9d3e01dd42"    + _P),
        ("Ginger Ale",      70.00, 24.00, _U + "1585238342024-8d4e7ed6c47b" + _P),
        ("Club Soda",       60.00, 20.00, _U + "1554786407-0e9d3e01dd42"    + _P),
        ("Iced Tea Bottle", 70.00, 24.00, _U + "1585238342024-8d4e7ed6c47b" + _P),
        ("Energy Drink",    95.00, 35.00, _U + "1554786407-0e9d3e01dd42"    + _P),
        ("Lemon Soda",      70.00, 24.00, _U + "1585238342024-8d4e7ed6c47b" + _P),
    ],
    "Snacks": [
        ("French Fries",       105.00, 40.00, _U + "1541014995-62e1e5e5c98e"    + _P),
        ("Onion Rings",        110.00, 42.00, _U + "1527477396000-e27163b481c2"  + _P),
        ("Nachos",             125.00, 48.00, _U + "1513456247-5ca8c49d4a46"    + _P),
        ("Garlic Bread",        95.00, 36.00, _U + "1541014995-62e1e5e5c98e"    + _P),
        ("Mozzarella Sticks",  125.00, 48.00, _U + "1527477396000-e27163b481c2"  + _P),
        ("Chicken Wings",      165.00, 65.00, _U + "1527477396000-e27163b481c2"  + _P),
        ("Potato Wedges",      110.00, 42.00, _U + "1541014995-62e1e5e5c98e"    + _P),
        ("Spring Rolls",       115.00, 44.00, _U + "1513456247-5ca8c49d4a46"    + _P),
        ("Cheese Balls",       110.00, 42.00, _U + "1527477396000-e27163b481c2"  + _P),
        ("Sweet Potato Fries", 115.00, 44.00, _U + "1541014995-62e1e5e5c98e"    + _P),
    ],
}

# ── Buying patterns: (items, weight) — higher weight = more frequent ─────────
# These drive the Apriori associations we want to see in the trained model.
COMBOS: list[tuple[list[str], int]] = [
    # Coffee + Dessert
    (["Espresso",       "Brownie"],           10),
    (["Latte",          "Cheesecake"],         9),
    (["Cappuccino",     "Chocolate Cake"],      8),
    (["Americano",      "Tiramisu"],            6),
    (["Caramel Latte",  "Macarons"],            6),
    (["Cold Brew",      "Red Velvet Cake"],     5),
    (["Flat White",     "Lemon Tart"],          5),
    (["Mocha",          "Chocolate Pudding"],   6),
    (["Iced Coffee",    "Brownie"],             5),
    (["Macchiato",      "Cream Puff"],          4),
    # Tea + Dessert
    (["Jasmine Tea",    "Cheesecake"],          8),
    (["Matcha Latte",   "Red Velvet Cake"],     6),
    (["Green Tea",      "Macarons"],            5),
    (["Taro Milk Tea",  "Cream Puff"],          6),
    (["Thai Iced Tea",  "Brownie"],             5),
    (["Earl Grey",      "Lemon Tart"],          5),
    (["Chamomile Tea",  "Chocolate Cake"],      4),
    (["Milk Tea",       "Macarons"],            5),
    (["Lemon Tea",      "Cheesecake"],          4),
    # Burger meals
    (["Classic Burger",        "French Fries",      "Coke"],          14),
    (["Cheeseburger",          "Onion Rings",        "Sprite"],        12),
    (["Chicken Burger",        "French Fries",       "Pepsi"],         12),
    (["Bacon Burger",          "Potato Wedges",      "Mountain Dew"],   7),
    (["BBQ Burger",            "Onion Rings",        "Root Beer"],      7),
    (["Crispy Chicken Burger", "Cheese Balls",       "Coke"],           8),
    (["Double Patty Burger",   "Sweet Potato Fries", "Energy Drink"],   5),
    (["Veggie Burger",         "French Fries",       "Lemonade"],       4),
    (["Mushroom Burger",       "Onion Rings",        "Sprite"],         5),
    (["Spicy Burger",          "French Fries",       "Coke"],           6),
    # Pizza meals
    (["Margherita Pizza",      "Garlic Bread", "Pepsi"],               10),
    (["Pepperoni Pizza",       "Garlic Bread", "Coke"],                11),
    (["BBQ Chicken Pizza",     "Mozzarella Sticks", "Mountain Dew"],    8),
    (["Hawaiian Pizza",        "Sprite"],                               7),
    (["Four Cheese Pizza",     "Garlic Bread", "Apple Juice"],          6),
    (["Meat Lovers Pizza",     "Garlic Bread", "Coke"],                 7),
    (["Buffalo Chicken Pizza", "Onion Rings",  "Pepsi"],                6),
    (["Veggie Supreme Pizza",  "Lemonade"],                             5),
    (["Italian Sausage Pizza", "Garlic Bread", "Root Beer"],            5),
    # Pasta meals
    (["Spaghetti Bolognese", "Garlic Bread", "Lemonade"],               9),
    (["Carbonara",           "Garlic Bread", "Orange Juice"],           9),
    (["Seafood Pasta",       "Lemonade"],                               5),
    (["Lasagna",             "Garlic Bread", "Apple Juice"],            6),
    (["Pesto Pasta",         "Orange Juice"],                           4),
    (["Mac and Cheese",      "Sprite"],                                 5),
    (["Alfredo Pasta",       "Garlic Bread", "Mango Juice"],            4),
    # Sandwich meals
    (["Club Sandwich",       "Orange Juice"],                           8),
    (["BLT Sandwich",        "Apple Juice"],                            7),
    (["Chicken Caesar Wrap", "Lemonade"],                               6),
    (["Turkey Sandwich",     "Ginger Ale"],                             5),
    (["Philly Cheesesteak",  "Coke"],                                   6),
    (["Monte Cristo",        "Orange Juice"],                           4),
    (["Tuna Melt",           "Apple Juice"],                            4),
    # Snack combos
    (["Chicken Wings",    "French Fries",  "Mountain Dew"],             9),
    (["Nachos",           "Coke"],                                      7),
    (["Spring Rolls",     "Lemon Tea"],                                 6),
    (["Mozzarella Sticks","Sprite"],                                    6),
    (["Cheese Balls",     "Lemon Tea"],                                 5),
    (["Potato Wedges",    "Pepsi"],                                     5),
    # Family/group
    (["Classic Burger","French Fries","Coke","Brownie"],                5),
    (["Pepperoni Pizza","Garlic Bread","Coke","Tiramisu"],              5),
    (["Chicken Wings","Nachos","French Fries","Mountain Dew"],          4),
    # Morning combos
    (["Espresso",      "Club Sandwich"],                                6),
    (["Latte",         "Egg Salad Sandwich"],                           5),
    (["Cappuccino",    "BLT Sandwich"],                                 5),
    (["Green Tea",     "Tuna Melt"],                                    4),
    # Juice + Snack
    (["Mango Juice",        "Onion Rings"],                             5),
    (["Orange Juice",       "French Fries"],                            5),
    (["Watermelon Juice",   "Spring Rolls"],                            4),
    (["Mixed Berry Juice",  "Macarons"],                                4),
]

# Build weighted combo pool
_COMBO_POOL: list[list[str]] = []
for items, weight in COMBOS:
    _COMBO_POOL.extend([items] * weight)


# ── Filipino customer name parts ──────────────────────────────────────────────
_FIRST = [
    "Maria", "Jose", "Juan", "Ana", "Sofia", "Carlos", "Miguel", "Gabriela",
    "Antonio", "Isabella", "Rafael", "Valentina", "Luis", "Camila", "Marco",
    "Andrea", "Diego", "Lucia", "Daniel", "Elena", "Ricardo", "Paula", "Eduardo",
    "Sara", "Fernando", "Melissa", "Roberto", "Christine", "Angelo", "Karen",
    "Mark", "Hazel", "Ryan", "Katrina", "Patrick", "Jasmine", "Michael", "Trisha",
    "John", "Maricel", "Paul", "Grace", "James", "Rose", "David", "Lovely",
    "Christian", "Joanna", "Francis", "Lea",
]
_LAST = [
    "Santos", "Reyes", "Garcia", "Cruz", "Torres", "Dela Cruz", "Flores",
    "Lopez", "Hernandez", "Ramos", "Gonzales", "Diaz", "Perez", "Bautista",
    "Mendoza", "Aquino", "Villanueva", "Castillo", "Santiago", "Lim",
    "Tan", "Go", "Sy", "Chua", "Ong", "Yap", "Co", "Chan", "Tan", "Wong",
    "Aguilar", "Evangelista", "Salazar", "Miranda", "Pascual", "Valdez",
    "Navarro", "Morales", "Soriano", "Macaraeg",
]


def _random_name() -> str:
    return f"{random.choice(_FIRST)} {random.choice(_LAST)}"


def _random_ts(start: datetime, end: datetime) -> datetime:
    """Random timestamp within [start, end] biased toward peak hours."""
    delta = int((end - start).total_seconds())
    while True:
        dt = start + timedelta(seconds=random.randint(0, delta))
        # Reject non-operating hours (closed 23:00-06:59)
        if dt.hour < 7 or dt.hour >= 23:
            continue
        # Peak-hour bias: roll again if not peak and random says no
        is_morning   = 8  <= dt.hour <= 10
        is_lunch     = 12 <= dt.hour <= 14
        is_evening   = 18 <= dt.hour <= 20
        is_peak = is_morning or is_lunch or is_evening
        if not is_peak and random.random() < 0.60:
            continue  # 60% chance to re-roll non-peak slots
        return dt


def _build_order_items(
    item_name_to_id: dict[str, int],
    item_name_to_price: dict[str, float],
) -> list[tuple[str, int, float, float]]:
    """Return a list of (name, menu_item_id, unit_price, subtotal)."""
    use_combo = random.random() < 0.72  # 72% of orders use a combo template

    if use_combo:
        base = random.choice(_COMBO_POOL)[:]
        # Occasionally add 1 random extra item
        if random.random() < 0.20:
            extras = [n for n in item_name_to_id if n not in base]
            if extras:
                base.append(random.choice(extras))
    else:
        n = random.randint(2, 5)
        base = random.sample(list(item_name_to_id.keys()), min(n, len(item_name_to_id)))

    lines = []
    for name in base:
        if name not in item_name_to_id:
            continue
        price = item_name_to_price[name]
        qty = 1
        lines.append((name, item_name_to_id[name], price, round(price * qty, 2)))
    return lines


# ── Main generator ────────────────────────────────────────────────────────────

def generate(n_customers: int = 200, n_orders: int = 2500) -> dict:
    """
    Insert demo data into the database.

    Returns a summary dict:
        {success, categories, menu_items, customers, orders, order_items, message}
    """
    # Use a single raw connection with buffered cursors to avoid
    # "Unread result found" errors in mysql-connector-python 9.x
    try:
        conn = _raw_conn(autocommit=False)
    except Exception as e:
        return {"success": False, "message": f"DB connection failed: {e}"}

    def cur():
        return conn.cursor(buffered=True)

    try:
        # Guard: don't re-insert if demo data already exists
        c = cur()
        c.execute("SELECT COUNT(*) FROM orders WHERE order_number LIKE 'DMO-%'")
        existing = c.fetchone()[0]
        c.close()
        if existing > 0:
            conn.close()
            return {
                "success": False,
                "message": f"Demo data already exists ({existing} orders found). "
                           "Delete it first using the Delete Demo Dataset button.",
            }

        cashier_ids = get_cashier_ids()
        start_dt = datetime(2024, 1, 1, 7, 0)
        end_dt   = datetime(2026, 6, 29, 22, 59)

        # ── Step 1: Insert/find categories ───────────────────────────────────
        cat_id_map: dict[str, int] = {}
        inserted_cats: list[str] = []

        c = cur()
        for name, desc, sort in DEMO_CATEGORIES:
            c.execute(
                "INSERT IGNORE INTO categories (name, description, sort_order) VALUES (%s,%s,%s)",
                (name, desc, sort),
            )
            c.execute("SELECT id FROM categories WHERE name = %s", (name,))
            row = c.fetchone()
            if row:
                cat_id_map[name] = row[0]
                inserted_cats.append(name)
        c.close()
        conn.commit()

        # ── Step 2: Insert menu items ─────────────────────────────────────────
        item_name_to_id:    dict[str, int]   = {}
        item_name_to_price: dict[str, float] = {}
        n_items_inserted = 0

        c = cur()
        for cat_name, products in DEMO_PRODUCTS.items():
            cat_id = cat_id_map.get(cat_name)
            if not cat_id:
                continue
            for pname, price, cost, img_url in products:
                desc = f"Demo item - {cat_name} category"
                c.execute(
                    """INSERT IGNORE INTO menu_items
                       (category_id, name, description, price, cost_price, image_path, is_available, is_active)
                       VALUES (%s,%s,%s,%s,%s,%s,1,1)""",
                    (cat_id, pname, desc, price, cost, img_url),
                )
                c.execute("SELECT id FROM menu_items WHERE name = %s", (pname,))
                row = c.fetchone()
                if row:
                    item_name_to_id[pname]    = row[0]
                    item_name_to_price[pname] = float(price)
                    n_items_inserted += 1
        c.close()
        conn.commit()

        if not item_name_to_id:
            conn.close()
            return {"success": False, "message": "Failed to insert menu items."}

        # ── Step 3: Insert customers ──────────────────────────────────────────
        customer_rows = []
        used_phones: set[str] = set()
        for i in range(n_customers):
            phone = f"0990{(i + 1):07d}"
            while phone in used_phones:
                phone = f"0990{random.randint(1000000, 9999999)}"
            used_phones.add(phone)
            name  = _random_name()
            email = f"demo{i + 1:03d}@demo.smartcafe.local"
            reg_date = (start_dt + timedelta(days=random.randint(0, 100))).date()
            customer_rows.append((name, phone, email, reg_date))

        c = cur()
        c.executemany(
            "INSERT IGNORE INTO customers (full_name, phone, email, created_at) VALUES (%s,%s,%s,%s)",
            customer_rows,
        )
        conn.commit()
        c.execute("SELECT id FROM customers WHERE email LIKE '%@demo.smartcafe.local' ORDER BY id")
        customer_ids = [r[0] for r in c.fetchall()]
        c.close()

        if not customer_ids:
            conn.close()
            return {"success": False, "message": "Failed to insert customers."}

        # Weighted customer pool: 15% regulars (10x), 35% frequent (4x), rest occasional (1x)
        n_regulars = max(1, int(len(customer_ids) * 0.15))
        n_frequent = max(1, int(len(customer_ids) * 0.35))
        weighted_cids: list[int] = []
        for i, cid in enumerate(customer_ids):
            if i < n_regulars:
                weighted_cids.extend([cid] * 10)
            elif i < n_regulars + n_frequent:
                weighted_cids.extend([cid] * 4)
            else:
                weighted_cids.append(cid)

        # ── Step 4: Generate order data in Python, then bulk-insert ──────────
        order_insert_rows: list[tuple] = []
        order_item_lines:  list[list]  = []   # parallel list of item lines per order
        date_counter: dict[str, int]   = {}

        for _ in range(n_orders):
            ts   = _random_ts(start_dt, end_dt)
            dkey = ts.strftime("%Y%m%d")
            seq  = date_counter.get(dkey, 0) + 1
            date_counter[dkey] = seq

            order_num  = f"DMO-{dkey}-{seq:04d}"
            cashier_id = random.choice(cashier_ids)
            cust_id    = random.choice(weighted_cids)

            lines = _build_order_items(item_name_to_id, item_name_to_price)
            if not lines:
                continue

            subtotal = round(sum(l[3] for l in lines), 2)
            tax      = round(subtotal * 0.12, 2)
            total    = round(subtotal + tax, 2)

            order_insert_rows.append((
                order_num, cashier_id, cust_id, "DINE_IN",
                "COMPLETED", subtotal, tax, 0.00, total, ts, ts,
            ))
            order_item_lines.append(lines)

        # Insert orders one-by-one to capture auto-generated IDs
        inserted_order_ids: list[int] = []
        c = cur()
        for row in order_insert_rows:
            c.execute(
                """INSERT INTO orders
                   (order_number, cashier_id, customer_id, order_type,
                    status, subtotal, tax, discount, total, created_at, updated_at)
                   VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)""",
                row,
            )
            inserted_order_ids.append(c.lastrowid)
        c.close()
        conn.commit()

        # Insert order items in batches of 500
        n_oi = 0
        oi_batch: list[tuple] = []
        c = cur()
        for oid, lines in zip(inserted_order_ids, order_item_lines):
            for (_, item_id, unit_price, sub) in lines:
                oi_batch.append((oid, item_id, 1, unit_price, sub, "SERVED"))
                n_oi += 1
            if len(oi_batch) >= 500:
                c.executemany(
                    """INSERT INTO order_items
                       (order_id, menu_item_id, quantity, unit_price, subtotal, status)
                       VALUES (%s,%s,%s,%s,%s,%s)""",
                    oi_batch,
                )
                oi_batch = []
        if oi_batch:
            c.executemany(
                """INSERT INTO order_items
                   (order_id, menu_item_id, quantity, unit_price, subtotal, status)
                   VALUES (%s,%s,%s,%s,%s,%s)""",
                oi_batch,
            )
        c.close()
        conn.commit()

        # ── Step 5: Update customer visit totals ──────────────────────────────
        c = cur()
        c.execute("""
            UPDATE customers ct
            JOIN (
                SELECT customer_id, COUNT(*) AS vc, SUM(total) AS ts
                FROM   orders
                WHERE  status = 'COMPLETED' AND customer_id IS NOT NULL
                GROUP  BY customer_id
            ) agg ON agg.customer_id = ct.id
            SET ct.visit_count = agg.vc,
                ct.total_spent = agg.ts
        """)
        c.close()
        conn.commit()

    except Exception as exc:
        try:
            conn.rollback()
        except Exception:
            pass
        conn.close()
        return {"success": False, "message": f"Generation failed: {exc}"}

    conn.close()

    # ── Step 6: Save metadata for clean deletion ──────────────────────────────
    meta = {
        "generated_at":          datetime.utcnow().isoformat(),
        "order_prefix":          "DMO",
        "customer_email_suffix": "@demo.smartcafe.local",
        "category_names":        inserted_cats,
        "n_customers":           len(customer_ids),
        "n_menu_items":          n_items_inserted,
        "n_orders":              len(inserted_order_ids),
        "n_order_items":         n_oi,
    }
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    META_FILE.write_text(json.dumps(meta, indent=2), encoding="utf-8")

    return {
        "success":     True,
        "categories":  len(inserted_cats),
        "menu_items":  n_items_inserted,
        "customers":   len(customer_ids),
        "orders":      len(inserted_order_ids),
        "order_items": n_oi,
        "message":     (
            f"Generated {len(customer_ids)} customers, "
            f"{n_items_inserted} menu items, "
            f"{len(inserted_order_ids)} orders ({n_oi} line items). "
            "Run 'Retrain AI' to train the recommendation engine."
        ),
    }


def delete_demo() -> dict:
    """Remove all demo data inserted by generate()."""
    try:
        conn = _raw_conn(autocommit=False)
        c = conn.cursor(buffered=True)

        c.execute("SELECT COUNT(*) FROM orders WHERE order_number LIKE 'DMO-%'")
        n_orders = c.fetchone()[0]
        c.execute("DELETE FROM orders WHERE order_number LIKE 'DMO-%'")

        c.execute("SELECT COUNT(*) FROM customers WHERE email LIKE '%@demo.smartcafe.local'")
        n_customers = c.fetchone()[0]
        c.execute("DELETE FROM customers WHERE email LIKE '%@demo.smartcafe.local'")

        n_items = 0
        n_cats  = 0
        if META_FILE.exists():
            meta = json.loads(META_FILE.read_text(encoding="utf-8"))
            cat_names = meta.get("category_names", [])
            if cat_names:
                ph = ",".join(["%s"] * len(cat_names))
                c.execute(f"SELECT id FROM categories WHERE name IN ({ph})", cat_names)
                cat_ids = [r[0] for r in c.fetchall()]
                if cat_ids:
                    ph2 = ",".join(["%s"] * len(cat_ids))
                    c.execute(f"SELECT COUNT(*) FROM menu_items WHERE category_id IN ({ph2})", cat_ids)
                    n_items = c.fetchone()[0]
                    c.execute(f"DELETE FROM menu_items WHERE category_id IN ({ph2})", cat_ids)
                    c.execute(f"DELETE FROM categories WHERE id IN ({ph2})", cat_ids)
                    n_cats = len(cat_ids)

        c.close()
        conn.commit()
        conn.close()

        if META_FILE.exists():
            META_FILE.unlink()

        return {
            "success":    True,
            "orders":     n_orders,
            "customers":  n_customers,
            "menu_items": n_items,
            "categories": n_cats,
            "message":    (
                f"Deleted {n_orders} demo orders, "
                f"{n_customers} demo customers, "
                f"{n_items} demo menu items, "
                f"{n_cats} demo categories."
            ),
        }
    except Exception as e:
        return {"success": False, "message": str(e)}


def demo_exists() -> bool:
    """Return True if demo data is present in the DB."""
    try:
        conn = _raw_conn(autocommit=True)
        c = conn.cursor(buffered=True)
        c.execute("SELECT COUNT(*) FROM orders WHERE order_number LIKE 'DMO-%'")
        n = c.fetchone()[0]
        c.close()
        conn.close()
        return n > 0
    except Exception:
        return False


if __name__ == "__main__":
    print("Generating demo data...")
    result = generate()
    print(json.dumps(result, indent=2))
