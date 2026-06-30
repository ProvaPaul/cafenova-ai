# SmartCafe AI — Training Report

**Generated:** 2026-06-30  
**Model:** Apriori Association Rules v1 (Groceries dataset)  
**Purpose:** Phase 2 validation — confirms the full AI pipeline works end-to-end

---

## Dataset Summary

| Metric                       | Value        |
|------------------------------|--------------|
| Source file                  | `Groceries_dataset.csv` |
| Total rows                   | 38,765       |
| Missing values               | 0            |
| Unique members               | 3,898        |
| Unique items                 | 167          |
| Member baskets (>=2 items)   | 3,892        |
| Avg unique items per member  | 8.93         |
| Date range                   | 2014-01-01 to 2015-12-30 |

---

## Preprocessing

| Step                          | Output                                |
|-------------------------------|---------------------------------------|
| Grouping strategy             | Member-level (all purchases = 1 basket) |
| One-hot encoding              | 3,892 × 167 bool DataFrame            |
| Encoded file size             | 120 KB (Parquet)                      |
| Preprocessing time            | 1.6 s                                 |

**Top 10 items by support:**

| Item              | Support |
|-------------------|---------|
| whole milk        | 0.459   |
| other vegetables  | 0.381   |
| rolls/buns        | 0.340   |
| soda              | 0.277   |
| yogurt            | 0.254   |
| root vegetables   | 0.205   |
| tropical fruit    | 0.202   |
| bottled water     | 0.184   |
| sausage           | 0.179   |
| citrus fruit      | 0.158   |

---

## Training Parameters

| Parameter         | Value  |
|-------------------|--------|
| Algorithm         | Apriori |
| min_support       | 0.02   |
| min_confidence    | 0.30   |
| max_len           | 3      |
| Library           | mlxtend 0.25.0 |

---

## Training Results

| Metric                | Value   |
|-----------------------|---------|
| Frequent itemsets     | 876     |
| Association rules     | 797     |
| Avg confidence        | 44.17%  |
| Avg lift              | 1.207   |
| Avg support           | 3.69%   |
| Max lift              | 1.563   |
| Min lift              | 1.000   |
| Training time         | 0.21 s  |
| Model file size       | 44 KB   |

---

## Top 10 Rules by Lift

| Antecedents                    | Consequents      | Confidence | Lift  | Support |
|--------------------------------|------------------|------------|-------|---------|
| brown bread + whole milk       | bottled water    | 33.5%      | 1.563 | 2.33%   |
| rolls/buns + yogurt            | sausage          | 32.0%      | 1.552 | 3.57%   |
| rolls/buns + sausage           | yogurt           | 43.3%      | 1.530 | 3.57%   |
| sausage + tropical fruit       | yogurt           | 43.2%      | 1.526 | 2.36%   |
| newspapers + rolls/buns        | root vegetables  | 35.0%      | 1.516 | 2.00%   |
| tropical fruit + yogurt        | sausage          | 31.2%      | 1.514 | 2.36%   |
| sugar                          | sausage          | 31.1%      | 1.511 | 2.05%   |
| other vegetables + yogurt      | sausage          | 30.9%      | 1.501 | 3.72%   |
| bottled water + yogurt         | sausage          | 30.9%      | 1.499 | 2.05%   |
| canned beer + rolls/buns       | root vegetables  | 34.2%      | 1.484 | 2.28%   |

---

## Lift Interpretation

- All 797 rules have **lift > 1.0**, meaning every rule represents a positive association
- No negative-lift rules are included (filtered by `min_lift=1.0` in the engine)
- The max lift of **1.563** means "brown bread + whole milk → bottled water"  
  is 56.3% more likely than buying bottled water by chance

---

## Live Test Results (2026-06-30)

```
GET /health
  ai_ready: true
  total_rules: 797
  status: ok

GET /api/v1/recommendations/trending?limit=3
  [bottled water (lift=1.56), sausage (lift=1.55), yogurt (lift=1.53)]

POST /api/v1/recommendations/recommend  items=["whole milk","yogurt"]
  [other vegetables (conf=47.7%), rolls/buns (conf=43.8%), soda (conf=36.1%)]

GET /api/v1/analytics/ai
  total_rules: 797
  avg_confidence: 0.4417
  avg_lift: 1.2068
  training_time_sec: 0.21
```

---

## Known Limitations (Phase 2)

1. **Domain mismatch** — rules are based on grocery items, not cafe menu items.  
   "whole milk" in this model is an ingredient, not a menu product.

2. **Lift ceiling** — max lift of 1.56 is modest. Real cafe order data is  
   expected to show stronger associations (e.g. coffee + croissant has very  
   high co-occurrence).

3. **Vocabulary gap** — item names from the menu (e.g. "Caramel Macchiato")  
   don't exist in this model's vocabulary. The engine falls back to trending.

---

## Next Steps

| Phase | Action                                              | Status     |
|-------|-----------------------------------------------------|------------|
| 2     | Validate pipeline using Groceries dataset           | DONE       |
| 3     | Export cafe Orders + OrderItems to CSV              | Waiting    |
| 3     | Run `preprocess_cafe.py` on exported data           | Waiting    |
| 3     | Train `apriori_cafe_v1` model                       | Waiting    |
| 3     | Deploy cafe model — remove demo_notice              | Waiting    |
| 4     | Self-learning AI (approval required before start)   | NOT STARTED |

> **Self-learning AI** (Phase 4) has NOT been approved and must NOT be  
> implemented until explicit approval is given.
