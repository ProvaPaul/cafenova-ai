# SmartCafe AI Service — API Documentation

**Base URL:** `http://localhost:8000/api/v1`  
**Interactive Docs:** `http://localhost:8000/docs`  
**ReDoc:** `http://localhost:8000/redoc`

---

## Health

### GET `/health`
Check service status and whether the AI model is loaded.

**Response**
```json
{
  "status": "ok",
  "service": "SmartCafe AI Service",
  "version": "1.0.0",
  "ai_ready": true,
  "total_rules": 797,
  "message": "Apriori model loaded: 797 rules (Groceries demo)."
}
```

---

## Recommendations

All recommendation endpoints include:
- `demo_notice` — reminder that this model uses the Groceries dataset
- `source` — `"apriori_groceries_v1"` (will change when cafe model is deployed)

### GET `/api/v1/recommendations/trending?limit=5`
Returns the top recommended items with no input required.  
Used by: **Home page (no login required)**, **Admin dashboard**.

**Response**
```json
{
  "success": true,
  "source": "apriori_groceries_v1",
  "demo_notice": "Demo AI — trained on public Groceries dataset.",
  "input_items": [],
  "recommendations": [
    {
      "recommendation": "bottled water",
      "confidence": 0.3346,
      "support": 0.0234,
      "lift": 1.5631,
      "reason": "Frequently Bought Together"
    }
  ],
  "total": 5,
  "generated_at": "2026-06-30T10:00:00Z"
}
```

---

### POST `/api/v1/recommendations/recommend`
**Main AI endpoint.** Given a list of items, returns items frequently bought together.  
Used by: **Cart page, Product detail, POS panel, Checkout.**

**Request**
```json
{
  "items": ["whole milk", "yogurt"],
  "limit": 5,
  "min_confidence": 0.30,
  "min_lift": 1.0
}
```

**Response** — same shape as `/trending`.

**Algorithm:**
1. For each association rule, check if `rule.antecedents ⊆ input_items`
2. Collect all matching consequents
3. Deduplicate by item name (keep highest-lift rule per item)
4. Sort by lift descending, then confidence descending
5. Return top `limit` results

---

### POST `/api/v1/recommendations/rules`
Returns the top association rules sorted by lift. Used by the **Admin AI Analytics page**.

**Request**
```json
{ "limit": 20 }
```

**Response**
```json
{
  "total_rules_in_model": 797,
  "showing": 20,
  "rules": [
    {
      "antecedents": ["brown bread", "whole milk"],
      "consequents": ["bottled water"],
      "support": 0.0234,
      "confidence": 0.3346,
      "lift": 1.5631,
      "reason": "Frequently Bought Together"
    }
  ]
}
```

---

### POST `/api/v1/recommendations/explain`
Explains why an item is recommended — shows which antecedents most strongly predict it.  
Used by: **Admin AI Analytics, future "Why this?" tooltip.**

**Request**
```json
{ "item": "whole milk" }
```

**Response**
```json
{
  "item": "whole milk",
  "total_rules": 42,
  "drivers": [
    {
      "if_you_buy": ["curd", "yogurt"],
      "confidence": 0.56,
      "lift": 1.48,
      "support": 0.031
    }
  ],
  "explanation": "'whole milk' appears as a consequent in 42 rules. The strongest predictor has lift=1.480."
}
```

---

### POST `/api/v1/recommendations/cart`
Legacy endpoint — accepts cart product names, returns recommendations.

**Request**
```json
{
  "product_ids": [35, 43],
  "product_names": ["Caramel Macchiato", "Brown Sugar Bubble Tea"],
  "limit": 5
}
```

---

### POST `/api/v1/recommendations/pos`
Legacy endpoint — for Cashier POS "You May Also Like" panel.

**Request**
```json
{
  "current_item_ids": [35],
  "current_item_names": ["Caramel Macchiato"],
  "limit": 4
}
```

---

## Analytics

### GET `/api/v1/analytics/ai`
**Real AI data** — model statistics from the trained Apriori model.

**Response**
```json
{
  "ready": true,
  "total_rules": 797,
  "avg_confidence": 0.4417,
  "avg_lift": 1.2068,
  "avg_support": 0.0369,
  "max_lift": 1.5631,
  "training_time_sec": 0.21,
  "dataset": "Groceries Dataset (public — Market Basket Analysis)",
  "algorithm": "apriori",
  "trained_at": "2026-06-30T15:56:30",
  "n_frequent_itemsets": 876,
  "top_rules": [ ... ],
  "note": "Trained on public Groceries dataset for validation."
}
```

### GET `/api/v1/analytics/dashboard`
Business KPI dashboard (stub — will use real DB queries in production).

### GET `/api/v1/analytics/sales-trends?days=14`
Historical + 7-day forecast (stub).

### GET `/api/v1/analytics/peak-hours`
Hourly order distribution (stub).

### GET `/api/v1/analytics/customer-segments`
Customer segmentation (stub).

---

## Recommendation Format Reference

Every recommendation object follows this exact schema:

| Field            | Type    | Description                                          |
|------------------|---------|------------------------------------------------------|
| `recommendation` | string  | Item name                                            |
| `confidence`     | float   | P(consequent \| antecedent) — range 0–1             |
| `support`        | float   | % of all baskets containing this item — range 0–1   |
| `lift`           | float   | How much more likely than chance — 1.0 = neutral     |
| `reason`         | string  | Human-readable label (see table below)               |

**Reason labels by lift range:**

| Lift          | Reason label                  |
|---------------|-------------------------------|
| ≥ 1.5         | Frequently Bought Together    |
| 1.3 – 1.5     | Popular Combination           |
| 1.1 – 1.3     | Customers Also Bought         |
| < 1.1         | Recommended for You           |
