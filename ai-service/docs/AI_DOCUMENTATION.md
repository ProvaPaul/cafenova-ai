# SmartCafe AI — Technical Documentation

## Overview

The SmartCafe AI Recommendation Engine uses **Market Basket Analysis** via the
**Apriori algorithm** to find items frequently purchased together.

**Phase:** Validation (public Groceries dataset)  
**Phase Next:** Production (Cafe Orders + OrderItems tables)

---

## Dataset

**Name:** Groceries Dataset (Market Basket Analysis)  
**Source:** Public / Kaggle  
**Location:** `archive/Groceries_dataset.csv`

| Attribute         | Value                    |
|-------------------|--------------------------|
| Total rows        | 38,765                   |
| Unique members    | 3,898                    |
| Unique items      | 167                      |
| Date range        | 2014-01-01 – 2015-12-30  |

**Top items by frequency:**
1. whole milk (25.9%)
2. other vegetables (21.5%)
3. rolls/buns (17.9%)
4. soda (13.4%)
5. yogurt (10.8%)

---

## Preprocessing Pipeline

**Script:** `ai-service/preprocess.py`

### Steps

1. **Load & Validate**
   - Read CSV, check for required columns (`Member_number`, `Date`, `itemDescription`)
   - Drop rows with missing values
   - Normalise item names to lowercase

2. **Transaction Grouping**
   - Group by `Member_number` — each member's full purchase history is one basket
   - Why member-level (not date-level)?  
     Date-level grouping produced baskets of ~2.6 items on average — too sparse
     for Apriori to find meaningful patterns.  
     Member-level grouping produces baskets of ~8.9 unique items, providing
     sufficient co-occurrence signal (797 rules at confidence ≥ 0.30).

3. **One-Hot Encoding**
   - Uses `mlxtend.preprocessing.TransactionEncoder`
   - Output: boolean DataFrame of 3,892 baskets × 167 items
   - RAM: ~0.6 MB (bool dtype, ~8× smaller than float64)

4. **Statistics**
   - Saved to `data/dataset_stats.json`

**Outputs:**
- `data/encoded_transactions.parquet` — encoded baskets (120 KB)
- `data/transactions.json` — raw transaction list
- `data/dataset_stats.json` — summary statistics

---

## Model Training

**Script:** `ai-service/train.py`  
**Algorithm:** Apriori → Association Rules  
**Library:** `mlxtend 0.23+`

### Parameters

| Parameter        | Value | Rationale                                      |
|------------------|-------|------------------------------------------------|
| `min_support`    | 0.02  | Item must appear in ≥2% of member baskets      |
| `min_confidence` | 0.30  | Rule fires correctly at least 30% of the time  |
| `max_len`        | 3     | Max 3 items per itemset (prevents combinatorial explosion) |

### Results

| Metric                | Value     |
|-----------------------|-----------|
| Frequent itemsets     | 876       |
| Association rules     | 797       |
| Avg confidence        | 44.2%     |
| Avg lift              | 1.207     |
| Max lift              | 1.563     |
| Training time         | ~0.2 s    |
| Model file size       | 44 KB     |

**Outputs:**
- `data/trained_rules.joblib` — serialised rules DataFrame (compress=3)
- `data/metadata.json` — training parameters and statistics

---

## Recommendation Engine

**File:** `ai-service/app/services/ai_engine.py`

### Architecture

```
Request (items=[])
       │
       ▼
ai_engine._Engine.recommend()
       │
       ├─ Filter rules: antecedents ⊆ input_items
       ├─ Collect consequents
       ├─ Deduplicate (best lift per unique item)
       ├─ Sort by lift desc, confidence desc
       └─ Return top N
```

### Singleton Pattern
The engine is loaded once at startup (`ensure_loaded()` in `main.py` lifespan).
Subsequent requests hit the in-memory DataFrame — no disk I/O per request.

### Fallback
If no matching rules are found for the input items (e.g. item names don't appear
in the Groceries vocabulary), the engine falls back to the global trending list
(top items by lift across all rules).

---

## Lift Interpretation

| Lift value | Meaning                                      |
|------------|----------------------------------------------|
| > 1.0      | Positive association — bought together more than by chance |
| = 1.0      | Independent — no association                 |
| < 1.0      | Negative association — rarely bought together |

All rules in the model have lift ≥ 1.0 (enforced by `min_lift=1.0` in `/recommend`).

---

## Memory & Performance

| Operation          | Time    | RAM delta |
|--------------------|---------|-----------|
| Preprocess         | ~1.6 s  | ~50 MB (released after encoding) |
| Train (Apriori)    | ~0.2 s  | ~15 MB   |
| Load model         | ~0.05 s | ~5 MB    |
| Single /recommend  | <5 ms   | 0 MB     |

The large `encoded` DataFrame is freed with `del encoded; gc.collect()` immediately
after Apriori completes — it is not kept in RAM at serve time.

---

## Upgrade Path: Groceries → Cafe Orders

This section explains how to replace the demo model with a production model
trained on the cafe's own data.

### Why replace?
The Groceries dataset contains grocery items (milk, bread, yogurt).  
The actual cafe menu contains drinks, pastries, and food items.  
The patterns are different — e.g. "Caramel Macchiato + Butter Croissant"  
will not appear in the Groceries rules.

### What to do

1. **Export cafe transaction data**
   ```sql
   SELECT o.id AS order_id, oi.product_id, mi.name AS item_name
   FROM orders o
   JOIN order_items oi ON oi.order_id = o.id
   JOIN menu_items mi  ON mi.id = oi.product_id
   WHERE o.status = 'COMPLETED'
   ORDER BY o.id;
   ```

2. **Create `preprocess_cafe.py`**
   - Group by `order_id` (one order = one basket)
   - Each item in the order is one transaction item
   - No need for member-level grouping — order baskets are already correct

3. **Train with adjusted parameters**
   - Cafe orders will have fewer items per basket (~3–6) than member-level grocery baskets
   - Lower `min_support` (e.g. 0.01) to compensate for smaller dataset
   - Keep `min_confidence = 0.30`, `min_threshold = 1.0`

4. **Save as `trained_rules_cafe.joblib`** and update `ai_engine.py` to load it

5. **No API changes needed** — all endpoints stay identical.  
   Only `source` field changes from `"apriori_groceries_v1"` to `"apriori_cafe_v1"`.

6. **Remove `demo_notice`** from responses once the cafe model is deployed.

> **Important:** Do NOT implement self-learning AI (reinforcement learning /
> online updates) until explicitly approved. The current architecture is
> batch-trained — retrain manually whenever new order data is available,
> or schedule a weekly retrain job.
