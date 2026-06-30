# User Manual

## Logging In

1. Launch the application
2. Enter your **Username** and **Password**
3. Click **Login**

If you forget your password, click **Forgot Password** to receive a reset token via the registered email.

---

## Role Guide

| Role | What you can access |
|---|---|
| **Admin** | Everything |
| **Manager** | Everything except Employees module |
| **Cashier** | POS, Products (view), Billing, Customers, Reservations |
| **Kitchen Staff** | Kitchen order display only |

---

## Dashboard Home

Shows live statistics when you log in:
- Total orders today
- Revenue today
- Low-stock alerts
- Pending reservations

---

## Categories

**Admin / Manager only**

| Action | How |
|---|---|
| Add category | Click **+ Add** → fill Name, Description, Sort Order → Save |
| Edit | Select row → click **Edit** |
| Delete | Select row → click **Delete** (only if no products use it) |
| Search | Type in the search box — filters as you type |

---

## Products (Menu Items)

| Action | How |
|---|---|
| Add product | Click **+ Add Product** → fill Name, Category, Price, Cost, Description → Save |
| Edit | Select product → click **Edit** |
| Toggle availability | Select product → click **Toggle Available** (hides from POS without deleting) |
| Delete | Select product → click **Delete** |
| Search | Use the search box or filter by category dropdown |

---

## POS (Point of Sale)

1. Browse products in the **left panel** (filter by category or search)
2. Click a product to **add it to the cart**
3. Adjust **quantity** in the cart or click the quantity cell to edit
4. Select **table** (for dine-in) or choose **Takeaway/Delivery**
5. Optionally enter **customer name**
6. Click **Place Order** → a payment dialog opens
7. Select **payment method** (Cash / Card / Mobile), enter amount → **Confirm Payment**
8. An invoice is generated (PDF) and the order is recorded

**Keyboard shortcut:** Press Enter in the search box to add the top result to the cart.

---

## Inventory

| Action | How |
|---|---|
| Add item | Click **+ Add Item** → fill Name, Unit, Stock, Min Stock, Cost → Save |
| Adjust stock | Select item → click **Adjust Stock** → enter adjustment amount |
| Low stock alert | Items with stock ≤ min stock appear highlighted in the table |
| Suppliers | Click the **Suppliers** tab to manage supplier records |

---

## Billing / Order History

- View all completed, cancelled, and pending orders
- Filter by date range or status
- Click **View Invoice** to open the PDF receipt
- Click **Reprint** to regenerate the invoice

---

## Customers

| Action | How |
|---|---|
| Add customer | Click **+ Add Customer** → fill Name, Phone, Email → Save |
| Edit | Select customer → click **Edit** |
| View history | Select customer → click **Purchase History** |
| Add loyalty points | Select customer → click **Add Points** → enter points |
| Search | Type name or phone in the search box |

**Loyalty points** are automatically credited when orders are linked to a customer (1 point per ₱10 spent).

---

## Employees (Admin only)

### Employees Tab
- Add, edit, and deactivate employee records
- Fields: Name, Position, Department, Base Salary, Hire Date, Phone, Email

### Attendance Tab
1. Select a **date** using the date spinner
2. Click **Mark Attendance**
3. Choose an employee from the list
4. Select status: Present / Absent / Late / Half Day / Leave

### Salary Tab
1. Select **month** and **year**
2. Click **Generate All** to create salary records for all active employees
3. Select a salary record → click **Mark as Paid** to confirm payment

---

## Reservations

| Action | How |
|---|---|
| Add reservation | Click **+ Add** → fill Customer Name, Date, Time, Party Size, Table, Notes |
| Confirm | Select pending reservation → click **Confirm** |
| Cancel | Select reservation → click **Cancel** |
| Complete | Select confirmed reservation → click **Complete** |
| Filter | Use the date range and status filters to narrow results |

---

## Reports

All report tabs have a **Load** button — click it to refresh data.

| Tab | What it shows |
|---|---|
| **Daily Sales** | Revenue and order count per day, bar chart |
| **Monthly Sales** | Monthly revenue trend, line chart |
| **Product Sales** | Best-selling items by revenue, pie chart |
| **Top Customers** | Customers ranked by total spending, bar chart |
| **AI Insights** | Menu recommendations, sales forecast, customer segments (demo) |

---

## AI Insights Tab

Click **Load AI Insights** to see:
- **Menu Recommendations** — products predicted to sell well
- **Sales Forecast** — estimated revenue for the next 7 days
- **Customer Insights** — loyalty segments (High Value, Loyal, At Risk, Churned)

> Currently shows **demo data**. Connect the Python FastAPI server and set `ai.enabled=true`
> in `src/main/resources/config/ai.properties` to enable live AI recommendations.

---

## Settings

### Appearance
- Toggle **Dark Mode** / **Light Mode** — applies instantly across all windows

### Database
- **Backup Database** — saves a `.sql` dump to a file you choose
- **Restore Database** — loads a `.sql` file back into the database

> Restore will overwrite all current data. Make a backup first.

### About
- Shows application version, developer info, and tech stack

---

## Signing Out

Click **Sign Out** in the top-right corner of any dashboard.
You will be returned to the login screen.
