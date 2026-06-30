# SmartCafe AI — User Manual

## For Admin Users

### AI Analytics Dashboard

Navigate to **Admin → AI Analytics** in the desktop application.

---

#### Step 1 — Generate Demo Dataset

Click **"Generate Demo Dataset"**.

The system will:
- Insert 10 menu categories (Coffee, Tea, Burger, Pizza, Pasta, Sandwich, Dessert, Juice, Soft Drinks, Snacks)
- Insert 100 menu items (10 per category)
- Insert 200 demo customers
- Insert ~2,500 completed orders with realistic buying patterns:
  - Coffee + Brownie
  - Classic Burger + French Fries + Coke
  - Pepperoni Pizza + Garlic Bread
  - Jasmine Tea + Cheesecake
  - and 65 more buying patterns

Wait for the success message before proceeding.

---

#### Step 2 — Retrain AI

Click **"Retrain AI"**.

The system will:
- Read all COMPLETED orders from the database
- Find which items are frequently purchased together
- Build an Apriori recommendation model
- Save the model and reload the engine automatically

You will see a confirmation with:
- Number of rules found (e.g. 312 rules)
- Average confidence and lift
- Training time

**The AI is now ready to make recommendations.**

---

#### Reading the Dashboard

After clicking **Refresh**, you will see:

| Metric           | Meaning                                              |
|------------------|------------------------------------------------------|
| AI Status        | Online = model loaded, Offline = not available       |
| Total Rules      | Number of "if A then B" patterns found               |
| Avg Confidence   | How often a rule is correct (higher = better)        |
| Avg Lift         | How much better than random chance (higher = better) |
| Training Time    | How long the last training took                      |
| Dataset Source   | Demo Data / Cafe Orders / Groceries CSV              |
| Model Version    | Timestamp-based version identifier                   |
| Training Sessions| Total number of times AI has been trained            |

**Top Association Rules table** — the strongest patterns found:
- "Espresso → Brownie" — 72% confident, lift 2.41
- "Classic Burger + French Fries → Coke" — 65% confident, lift 2.14

**Training History table** — log of every training run with results.

---

#### Step 3 — Transition to Real Data

Once real customers start placing orders:

1. Click **"Delete Demo Dataset"** — removes all generated data
2. Click **"Retrain AI"** — trains from real orders only
3. The AI now reflects your actual customers' buying habits

---

#### Export AI Report

Click **"Export AI Report"** to save a full JSON report containing:
- Model statistics
- Training history
- Top 20 association rules

---

## For Cashiers — POS

When adding items to the cart, the **"You May Also Like"** panel appears on the right.

- Based on the current cart contents
- Shows top 4 recommendations
- Includes a reason: "Frequently Bought Together", "Customers Also Bought", etc.
- Click "+" to add the item directly to the cart

If the AI service is offline, the panel is hidden automatically.

---

## For Customers — Website

### Home Page
"Recommended For You" section shows trending items.

### Product Detail
"Customers Also Bought" section recommends items commonly purchased with the current product.

### Cart
"You May Also Like" recommendations based on your cart contents.

### Checkout
Final recommendations before payment.

### Order Success
"Order Again?" shows your previously purchased items.

---

## How the AI Learns

```
Customer Orders
       ↓
MySQL Database (orders + order_items)
       ↓
Apriori Algorithm finds patterns:
  "52% of customers who ordered Espresso also ordered Brownie"
       ↓
Recommendation Engine
  "You ordered Espresso → we recommend Brownie"
       ↓
Admin clicks Retrain → AI improves with new data
```

The more completed orders exist in the database, the better the recommendations become.

---

## FAQ

**Q: How often should I retrain the AI?**  
A: Recommended: once a week, or whenever you add new menu items. The AI learns from all COMPLETED orders each time.

**Q: What if recommendations seem wrong?**  
A: Click "Retrain AI" — if you just added new menu items, the AI needs to train on orders containing them.

**Q: Can I keep the demo data alongside real orders?**  
A: Yes — the AI will learn from both. Delete demo data when you have enough real orders (recommended: 500+ real orders).

**Q: What does "lift" mean?**  
A: Lift tells you how much more likely customers are to buy item B when they've already ordered item A, compared to buying B randomly. Lift > 1.5 means "Frequently Bought Together".

**Q: What is the minimum number of orders needed?**  
A: At least 50–100 completed orders for basic patterns. 500+ orders for strong, reliable recommendations.
