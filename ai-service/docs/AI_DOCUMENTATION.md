# SmartCafe AI — Technical Documentation v2.0

## Architecture Overview

```
MySQL Database
  └─ orders (COMPLETED)
       └─ order_items → menu_items
                │
                ▼
    Transaction Builder  (app/db/transaction_builder.py)
    [order_id → [item_name, item_name, ...]]
                │
                ▼
    One-Hot Encoding  (mlxtend.TransactionEncoder)
    [2500 baskets × 100 items boolean DataFrame]
                │
                ▼
    Apriori Algorithm  (mlxtend.frequent_patterns.apriori)
    min_support=0.02, max_len=3
                │
                ▼
    Association Rules  (mlxtend.frequent_patterns.association_rules)
    metric="confidence", min_threshold=0.30
                │
                ▼
    trained_rules.joblib (versioned)  ←──── Loaded by ai_engine.py
                │
                ▼
    FastAPI Recommendation Engine
    /recommend  /trending  /personal/{id}  /explain  /rules
```

---

## Dataset Phases

| Phase | Source                    | Model name            | Status      |
|-------|---------------------------|-----------------------|-------------|
| 1     | Groceries CSV (public)    | apriori_groceries_v1  | Validation  |
| 2     | Cafe Demo Data (generated)| cafe_v{timestamp}     | Demo        |
| 3     | Real Cafe Orders          | cafe_v{timestamp}     | Production  |

**No code changes are needed between phases.** Only the model file changes.

---

## Demo Data Generation

**Script:** `ai-service/scripts/generate_demo_data.py`

Inserts into MySQL:
- 10 categories: Coffee, Tea, Burger, Pizza, Pasta, Sandwich, Dessert, Juice, Soft Drinks, Snacks
- 100 menu items: 10 per category with realistic prices
- 200 customers: Filipino names, unique phones, `@demo.smartcafe.local` emails
- ~2 500 COMPLETED orders with 2–6 items each

**Buying patterns (69 combo templates):**
- 72% of orders use a buying pattern template (creates strong associations)
- 28% use random items (realistic noise)

**Example learned associations:**
- Espresso → Brownie (confidence ≈ 72%, lift ≈ 2.4)
- Classic Burger → French Fries → Coke
- Pepperoni Pizza → Garlic Bread
- Spaghetti Bolognese → Garlic Bread → Lemonade
- Jasmine Tea → Cheesecake

**Demo data markers (for clean deletion):**
- Orders: `order_number LIKE 'DMO-%'`
- Customers: `email LIKE '%@demo.smartcafe.local'`

---

## Training Pipeline

**Script:** `ai-service/scripts/train_from_db.py`

### Step 1 — Load Transactions from MySQL
```sql
SELECT o.id AS order_id, mi.name AS item_name
FROM   orders o
JOIN   order_items  oi ON oi.order_id    = o.id
JOIN   menu_items   mi ON mi.id          = oi.menu_item_id
WHERE  o.status = 'COMPLETED'
ORDER  BY o.id, mi.name
```
Each completed order becomes one basket. Items within an order are grouped by `order_id`.

### Step 2 — One-Hot Encoding
`TransactionEncoder.fit_transform()` → boolean DataFrame

### Step 3 — Apriori
```python
freq_items = apriori(encoded, min_support=0.02, use_colnames=True, max_len=3)
rules = association_rules(freq_items, metric="confidence", min_threshold=0.30)
rules = rules.sort_values("lift", ascending=False)
```

**Adaptive min_support:** if `n_transactions < 200`, min_support is halved to find patterns in smaller datasets.

### Step 4 — Save Versioned Model
- New file: `data/models/cafe_v{YYYYMMDD_HHMMSS}_rules.joblib`
- Latest copy: `data/trained_rules.joblib` (always points to newest)
- Metadata: `data/metadata.json`
- DB log: `ai_training_sessions` table

### Step 5 — Hot Reload
After saving, `admin.py` resets `engine._ready = False` and calls `ensure_loaded()`.
No server restart needed.

---

## Recommendation Engine

**File:** `ai-service/app/services/ai_engine.py`

### Singleton Pattern
```python
_engine = _Engine()   # module-level singleton

def get_engine() -> _Engine:
    return _engine

def ensure_loaded() -> bool:
    return _engine.load()   # no-op if already loaded
```

### recommend() Algorithm
```
Given input_items = ["Classic Burger", "French Fries"]

For each rule in trained_rules:
    if rule.antecedents ⊆ input_items
    AND rule.confidence >= min_confidence
    AND rule.lift >= min_lift:
        add rule.consequents to candidates

Deduplicate by item name (keep highest-lift rule per item)
Sort by lift desc, then confidence desc
Return top N
```

### Fallback
If no matching rules found → fall back to `trending()` (global top items by lift).

---

## Personalisation Service

**File:** `ai-service/app/services/personalization.py`

Queries per customer:
1. **Recent items** — last 15 distinct items from last 10 orders
2. **Favourite items** — top 10 items by order count
3. **Favourite categories** — top 5 categories by order count
4. **Visit count** — total completed orders
5. **Time profile** — morning/afternoon/evening visit distribution

**Reason priority:**
1. `"You frequently order Espresso"` (if item in top favourites, ≥3 times)
2. `"You love Coffee items"` (if item from favourite category)
3. `"Popular morning choice"` (time-based, if time profile shows morning visits)
4. Standard Apriori reason (Frequently Bought Together, etc.)

**New customer fallback** (< 3 orders):
- Returns trending items
- Reason: `"Trending Now"`

---

## Model Versioning

**File:** `ai-service/app/services/model_registry.py`

```
data/
  models/
    cafe_v20260630_120000_rules.joblib   ← v1
    cafe_v20260630_162300_rules.joblib   ← v2 (active)
  trained_rules.joblib                   ← copy of latest
  metadata.json                          ← metadata for latest
```

All training sessions also logged to `ai_training_sessions` MySQL table:
- version, dataset_source, n_rules, avg_confidence, avg_lift, training_time_sec, trained_at

---

## Training Parameters

| Parameter        | Default | Notes                                        |
|------------------|---------|----------------------------------------------|
| `min_support`    | 0.02    | Adaptive: halved if < 200 transactions       |
| `min_confidence` | 0.30    | Rules fire correctly ≥30% of the time         |
| `max_len`        | 3       | Max itemset size (prevents explosion)        |
| `min_lift`       | 1.0     | Applied at serve time per request            |

Configurable via `.env`:
```
TRAIN_MIN_SUPPORT=0.02
TRAIN_MIN_CONFIDENCE=0.30
TRAIN_MAX_LEN=3
```

---

## Lift Interpretation

| Lift   | Meaning                                      |
|--------|----------------------------------------------|
| > 2.0  | Very strong positive association              |
| 1.5–2.0| Strong positive association                  |
| 1.1–1.5| Moderate positive association                |
| 1.0    | Independent — no association                 |
| < 1.0  | Negative — bought less often together        |

**Demo data expected lift range:** 1.5–3.0 (strong, because combos are explicit)  
**Real cafe orders expected lift range:** 1.2–2.5

---

## Scheduled Auto-Retrain

Configured via `.env`:
```
AUTO_RETRAIN_HOUR=3   # 3am daily. Set to 0 to disable.
```

Uses `APScheduler` (AsyncIOScheduler). Requires `apscheduler>=3.10.4`.
If not installed, startup proceeds without scheduling.

---

## Transition: Demo → Production

| Step | Action                                   | Code change needed? |
|------|------------------------------------------|---------------------|
| 1    | Admin clicks "Delete Demo Dataset"       | None                |
| 2    | Real customers place orders via POS      | None                |
| 3    | Admin clicks "Retrain AI"                | None                |
| 4    | Engine reloads from new model            | None                |
| 5    | `demo_notice` becomes empty string       | None                |
| 6    | `source` field shows `cafe_v{timestamp}` | None                |

**The API interface is completely stable across all phases.**
