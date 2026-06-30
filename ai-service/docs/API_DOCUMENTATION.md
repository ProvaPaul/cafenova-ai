# SmartCafe AI Service — API Documentation v2.0

**Base URL:** `http://localhost:8000/api/v1`  
**Interactive Docs:** `http://localhost:8000/docs`  
**Service Version:** 2.0.0

---

## Health

### GET `/health`
```json
{
  "status": "ok",
  "service": "SmartCafe AI Service",
  "version": "2.0.0",
  "ai_ready": true,
  "total_rules": 312,
  "message": "Model loaded: 312 rules  version=cafe_v20260630_162300  source=DEMO_DATA"
}
```

---

## Recommendations

Every recommendation object follows this schema:

| Field            | Type   | Description                               |
|------------------|--------|-------------------------------------------|
| `recommendation` | string | Item name                                 |
| `confidence`     | float  | P(item\|antecedents) — 0–1              |
| `support`        | float  | Basket frequency — 0–1                   |
| `lift`           | float  | Lift ratio — 1.0 = neutral               |
| `reason`         | string | Human-readable explanation                |

**Reason labels by lift:**

| Lift         | Reason                         |
|--------------|-------------------------------|
| ≥ 1.5        | Frequently Bought Together    |
| 1.3 – 1.5    | Popular Combination           |
| 1.1 – 1.3    | Customers Also Bought         |
| < 1.1        | Recommended for You           |

---

### GET `/api/v1/recommendations/trending?limit=5`
Top items — no input required. Used by Home page.

**Response**
```json
{
  "success": true,
  "source": "cafe_v20260630_162300",
  "demo_notice": "Trained on demo dataset.",
  "input_items": [],
  "recommendations": [
    { "recommendation": "Brownie",      "confidence": 0.72, "support": 0.12, "lift": 2.41, "reason": "Frequently Bought Together" },
    { "recommendation": "French Fries", "confidence": 0.68, "support": 0.18, "lift": 2.14, "reason": "Frequently Bought Together" },
    { "recommendation": "Garlic Bread", "confidence": 0.65, "support": 0.15, "lift": 1.98, "reason": "Frequently Bought Together" }
  ],
  "total": 3,
  "generated_at": "2026-06-30T10:00:00Z"
}
```

---

### POST `/api/v1/recommendations/recommend`
**Main endpoint.** Items in → recommendations out.

**Request**
```json
{
  "items": ["Classic Burger", "French Fries"],
  "limit": 5,
  "min_confidence": 0.25,
  "min_lift": 1.0
}
```

**Response** — same shape as `/trending`.

---

### GET `/api/v1/recommendations/personal/{customer_id}?limit=5`
**Personalised** — uses order history from MySQL.

Returns personalised reasons:
- `"You frequently order Espresso"`
- `"You love Coffee items"`
- `"Popular morning choice"`
- `"Customers who ordered similar items also loved this"`

Falls back to trending if customer has < 3 orders.

**Response extra fields**
```json
{
  "extra": {
    "is_personalized": true,
    "visit_count": 15,
    "time_slot": "morning",
    "fallback_reason": ""
  }
}
```

---

### POST `/api/v1/recommendations/rules`
Top association rules by lift — for Admin AI Analytics table.

**Request** `{ "limit": 20 }`

**Response**
```json
{
  "total_rules_in_model": 312,
  "showing": 20,
  "rules": [
    {
      "antecedents": ["Espresso"],
      "consequents": ["Brownie"],
      "support": 0.118,
      "confidence": 0.722,
      "lift": 2.410,
      "reason": "Frequently Bought Together"
    }
  ]
}
```

---

### POST `/api/v1/recommendations/explain`
Why is an item recommended? Strongest predictors.

**Request** `{ "item": "Brownie" }`

**Response**
```json
{
  "item": "Brownie",
  "total_rules": 24,
  "drivers": [
    { "if_you_buy": ["Espresso"], "confidence": 0.72, "lift": 2.41, "support": 0.118 },
    { "if_you_buy": ["Latte"],    "confidence": 0.68, "lift": 2.28, "support": 0.103 }
  ],
  "explanation": "'Brownie' appears in 24 rules. Strongest predictor lift=2.410."
}
```

---

## Analytics

### GET `/api/v1/analytics/ai`
Real model statistics — used by Admin AI Analytics panel.

```json
{
  "ready": true,
  "total_rules": 312,
  "avg_confidence": 0.612,
  "avg_lift": 1.842,
  "max_lift": 2.734,
  "training_time_sec": 3.2,
  "dataset": "Cafe Demo Dataset (generated)",
  "dataset_source": "DEMO_DATA",
  "algorithm": "apriori",
  "version": "cafe_v20260630_162300",
  "trained_at": "2026-06-30T16:23:00",
  "top_rules": [ ... ]
}
```

---

## Admin Endpoints

### POST `/api/v1/admin/generate-demo`
Insert 200 customers, 100 menu items, ~2 500 COMPLETED orders.

**Response**
```json
{
  "success": true,
  "categories":  10,
  "menu_items":  100,
  "customers":   200,
  "orders":      2500,
  "order_items": 9832,
  "message": "Generated ... Run 'Retrain AI' to train the recommendation engine."
}
```

---

### POST `/api/v1/admin/retrain`
Train Apriori from COMPLETED orders in MySQL. Hot-reloads the engine.

**Response**
```json
{
  "success": true,
  "version": "cafe_v20260630_162300",
  "dataset_source": "DEMO_DATA",
  "n_transactions": 2500,
  "n_unique_items": 100,
  "n_frequent_itemsets": 284,
  "n_rules": 312,
  "avg_confidence": 0.612,
  "avg_lift": 1.842,
  "training_time_sec": 3.2,
  "engine_reloaded": true,
  "message": "Training complete in 3.5s. 312 rules from 2500 orders."
}
```

---

### DELETE `/api/v1/admin/demo-data`
Remove all demo data (orders with `DMO-` prefix, demo customers, demo menu items).

**Response**
```json
{
  "success": true,
  "orders": 2500,
  "customers": 200,
  "menu_items": 100,
  "categories": 10,
  "message": "Deleted 2500 demo orders, 200 demo customers, 100 demo menu items, 10 demo categories."
}
```

---

### GET `/api/v1/admin/training-history?limit=20`
List recent training sessions from `ai_training_sessions` table.

---

### GET `/api/v1/admin/ai-status`
AI engine + DB health check.

---

### GET `/api/v1/admin/export-report`
Full JSON report for download (all model stats + history + top rules).
