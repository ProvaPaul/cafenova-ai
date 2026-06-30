# Smart AI-Based Cafe Management System

A full-featured desktop application for managing a cafe — built with Java 17 Swing, MySQL 8, and a clean MVC + DAO architecture. Designed to grow into an AI-powered system via a Python FastAPI integration layer.

---

## Features

| Module | Capabilities |
|---|---|
| **Authentication** | Login, role-based access (Admin / Manager / Cashier / Kitchen Staff), password reset |
| **Categories** | Add, edit, delete menu categories with sort order |
| **Products** | Full menu management — price, cost, availability, category assignment |
| **POS** | Point-of-sale terminal — cart, order types, live table selection |
| **Inventory** | Stock tracking, low-stock alerts, supplier management |
| **Billing** | Order history, payment methods (Cash / Card / Mobile), PDF invoice |
| **Customers** | CRM — loyalty points, purchase history, visit tracking |
| **Employees** | CRUD, attendance tracking, salary generation and payment |
| **Reservations** | Table booking, status management (Pending → Confirmed → Completed) |
| **Reports** | Daily sales, monthly trends, product performance, top customers — JFreeChart |
| **AI Insights** | Menu recommendations, sales forecast, customer segmentation (demo stub; live via FastAPI) |
| **Settings** | Dark/light mode toggle, database backup and restore |

---

## Technology Stack

| Layer | Technology |
|---|---|
| UI Framework | Java Swing + FlatLaf 3.4.1 |
| Language | Java 17 |
| Build | Apache Maven 3.x (fat JAR) |
| Database | MySQL 8.x via XAMPP |
| Connection Pool | HikariCP 5.1.0 |
| Password Hashing | jBCrypt 0.4 |
| Charts | JFreeChart 1.5.4 |
| PDF Invoices | Apache PDFBox 2.0.30 |
| QR Codes | ZXing 3.5.2 |
| AI Layer (future) | Python FastAPI + REST (prepared, not yet live) |

---

## Architecture

```
Java Swing (Desktop UI)
       │
       ▼
Service Layer (Business Logic)
       │
       ▼
DAO Layer (Database Access — HikariCP → MySQL)
       │
       ▼
       MySQL 8.x
```

Future AI integration path:
```
Java Swing  ──►  RestApiClient  ──►  Python FastAPI  ──►  AI/ML Models
                 (ai.client)         (localhost:8000)     (Scikit-learn / TensorFlow)
```

---

## Quick Start

See [docs/INSTALLATION.md](docs/INSTALLATION.md) for the full guide.

**Short version:**
1. Install XAMPP (MySQL on port 3307)
2. Create `smart_cafe_db` and run `src/main/resources/db/schema.sql`
3. Insert default users (see [docs/DATABASE_SETUP.md](docs/DATABASE_SETUP.md))
4. `mvn clean package`
5. `java -jar target/smart-cafe-management-jar-with-dependencies.jar`

**Default login credentials:**

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Manager | `manager` | `manager123` |
| Cashier | `cashier` | `cashier123` |
| Kitchen | `kitchen` | `kitchen123` |

---

## Role Permissions

| Feature | Admin | Manager | Cashier | Kitchen |
|---|:---:|:---:|:---:|:---:|
| Categories | ✅ | ✅ | — | — |
| Products | ✅ | ✅ | view | — |
| POS | ✅ | ✅ | ✅ | — |
| Inventory | ✅ | ✅ | — | — |
| Billing | ✅ | ✅ | ✅ | — |
| Customers | ✅ | ✅ | ✅ | — |
| Employees | ✅ | — | — | — |
| Reservations | ✅ | ✅ | ✅ | — |
| Reports | ✅ | ✅ | — | — |
| Settings | ✅ | ✅ | — | — |
| Kitchen View | — | — | — | ✅ |

---

## Project Structure

```
src/main/java/com/smartcafe/
├── Main.java                   Entry point & dependency wiring
├── config/                     AppConfig, AppContext, DatabaseConfig
├── ai/                         AI integration layer (interface + stub + REST client)
│   ├── AiRecommendationService.java
│   ├── AiRecommendationServiceStub.java
│   ├── client/RestApiClient.java
│   ├── config/AiConfig.java
│   └── dto/                    MenuRecommendationDto, SalesForecastDto, CustomerInsightDto
├── controller/                 AuthController
├── dao/                        DAO interfaces
├── dao/impl/                   DAO implementations (JDBC + HikariCP)
├── exception/                  AppException, AuthException, DatabaseException, ValidationException
├── model/                      Domain models (User, Product, Order, Customer, …)
├── service/                    Service interfaces
├── service/impl/               Service implementations
├── util/                       PasswordUtil, PdfInvoiceUtil, QrCodeUtil, SessionManager, …
└── view/
    ├── MainFrame.java
    ├── auth/                   LoginPanel, SignupPanel, ForgotPasswordPanel
    ├── components/             RoundedButton, SidebarButton, CardPanel
    ├── dashboard/              AdminDashboard, ManagerDashboard, CashierDashboard, KitchenDashboard
    └── panel/                  All functional screens (POS, Inventory, Reports, …)
```

---

## Future Enhancements

- **Customer Website** — Online menu browsing, table reservation, loyalty point balance
- **AI Recommendation Engine** — Python FastAPI + scikit-learn for demand forecasting and menu recommendations
- **Mobile App** — Android/iOS companion app for managers
- **Kitchen Display System** — Real-time order screen for kitchen staff
- **Multi-branch Support** — Central dashboard for multiple cafe locations

---

## License

MIT — free to use, modify, and distribute.
