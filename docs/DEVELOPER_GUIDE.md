# Developer Guide

## Project Setup

See [INSTALLATION.md](INSTALLATION.md) for environment setup.

**Quick build:**
```bash
mvn clean package -DskipTests
java -jar target/smart-cafe-management-jar-with-dependencies.jar
```

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                    │
│   MainFrame  →  Dashboard (Admin/Manager/Cashier/       │
│                 Kitchen)  →  Panel screens               │
│                 AuthController                           │
└────────────────────────┬────────────────────────────────┘
                         │ AppContext (service locator)
┌────────────────────────▼────────────────────────────────┐
│                     SERVICE LAYER                        │
│   Business logic — validation, calculations, workflow   │
│   All service classes implement an interface             │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                      DAO LAYER                           │
│   JDBC + HikariCP — SQL queries, result mapping          │
│   All DAO classes implement an interface                 │
└────────────────────────┬────────────────────────────────┘
                         │
                    MySQL 8.x
```

### Key design decisions

- **No Spring** — pure Java + manual dependency injection via `Main.java`
- **`AppContext`** is a static service locator (not IoC container). Services are wired once in `Main.main()` and stored as `volatile` statics
- **HikariCP** manages the connection pool. All DAOs call `DatabaseConfig.getConnection()` per method and close it in a try-with-resources block
- **Records** (Java 17) used for all report row types in `ReportService`
- **FlatLaf** handles the entire look-and-feel. Dark/light mode is toggled by calling `FlatLightLaf.setup()` / `FlatDarkLaf.setup()` + `SwingUtilities.updateComponentTreeUI(window)`

---

## Package Layout

```
com.smartcafe
├── Main                       Startup, dependency wiring
├── config/
│   ├── AppConfig              Colors, fonts, constants
│   ├── AppContext             Static service locator
│   └── DatabaseConfig         HikariCP pool, testConnection()
├── ai/
│   ├── AiRecommendationService     Interface
│   ├── AiRecommendationServiceStub Demo implementation
│   ├── client/RestApiClient        Java HttpClient wrapper for FastAPI
│   ├── config/AiConfig             Reads ai.properties
│   └── dto/                        MenuRecommendationDto, SalesForecastDto, CustomerInsightDto
├── controller/
│   └── AuthController         Login → Dashboard routing
├── dao/                       Interfaces: UserDao, ProductDao, OrderDao, …
├── dao/impl/                  JDBC implementations
├── exception/
│   ├── AppException           Root exception
│   ├── AuthException          Login/permission failures
│   ├── DatabaseException      Wraps SQLException
│   └── ValidationException    Input validation failures
├── model/                     Domain objects: User, Product, Order, Customer, …
├── service/                   Business logic interfaces
├── service/impl/              Business logic implementations
├── util/
│   ├── PasswordUtil           BCrypt hash/verify
│   ├── PdfInvoiceUtil         PDFBox invoice generation
│   ├── QrCodeUtil             ZXing QR code
│   ├── SessionManager         Current logged-in user
│   ├── TokenUtil              Password reset tokens
│   └── ValidationUtil         Email, phone, name validation
└── view/
    ├── MainFrame              Single JFrame container (CardLayout)
    ├── auth/                  Login, Signup, ForgotPassword panels
    ├── components/            RoundedButton, SidebarButton, CardPanel
    ├── dashboard/             Role dashboards (AdminDashboard, …)
    └── panel/                 All functional screens
```

---

## Adding a New Module

1. **Model** — add `src/main/java/com/smartcafe/model/MyEntity.java`
2. **DAO interface** — `dao/MyEntityDao.java` with CRUD methods
3. **DAO implementation** — `dao/impl/MyEntityDaoImpl.java` (JDBC)
4. **Service interface** — `service/MyEntityService.java`
5. **Service implementation** — `service/impl/MyEntityServiceImpl.java`
6. **Wire in `AppContext`** — add a `volatile` field + accessor
7. **Wire in `Main.java`** — instantiate DAO → Service → `AppContext.initializeStepN()`
8. **UI Panel** — `view/panel/MyEntityPanel.java` (JPanel with loadData() method)
9. **Dashboard** — add the panel to `AdminDashboard`, `ManagerDashboard`, or `CashierDashboard`

---

## Exception Handling Pattern

All service methods follow this pattern:

```java
public List<MyEntity> findAll() {
    try {
        return myEntityDao.findAll();
    } catch (Exception e) {
        throw new AppException("Failed to load entities: " + e.getMessage(), e);
    }
}
```

UI panels catch `AppException` and show `JOptionPane.showMessageDialog(...)`.

Do NOT catch `Exception` silently — always wrap and re-throw with context.

---

## Adding AI Integration (Future Step)

When the Python FastAPI server is ready:

1. **Create** `AiRecommendationServiceImpl.java` implementing `AiRecommendationService`
2. Inject a `RestApiClient` into its constructor
3. Parse JSON responses from FastAPI (use `org.json` or Jackson — add to pom.xml)
4. Set `ai.enabled=true` in `src/main/resources/config/ai.properties`
5. In `Main.java`, swap the stub:
   ```java
   // Before (stub):
   AppContext.initializeStep5(new AiRecommendationServiceStub());

   // After (live):
   AppContext.initializeStep5(new AiRecommendationServiceImpl(new RestApiClient()));
   ```
6. The `ReportPanel → AI Insights` tab will automatically show live data

### Expected FastAPI endpoints

```
GET  /api/v1/health
GET  /api/v1/recommendations/menu
GET  /api/v1/forecast/sales?days=7
GET  /api/v1/insights/customers?limit=10
```

---

## Database Schema

See [DATABASE_SETUP.md](DATABASE_SETUP.md) and `src/main/resources/db/schema.sql`.

All DAO implementations use `DatabaseConfig.getConnection()`:

```java
try (Connection c = DatabaseConfig.getConnection();
     PreparedStatement ps = c.prepareStatement("SELECT ...")) {
    ps.setInt(1, id);
    try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) { ... }
    }
}
```

Connection pooling is transparent — HikariCP returns a pooled connection on each call.

---

## Configuration Files

| File | Purpose |
|---|---|
| `src/main/resources/config/database.properties` | MySQL connection + HikariCP pool settings |
| `src/main/resources/config/ai.properties` | AI FastAPI URL + enable/disable flag |
| `src/main/resources/db/schema.sql` | Full database schema + seed data |

User preferences (dark mode) are saved to `~/.smartcafe/settings.properties`.

---

## Building a Release

```bash
mvn clean package -DskipTests
# Output: target/smart-cafe-management-jar-with-dependencies.jar
# Distribute this single JAR — no other files needed on the target machine
```

Runtime requirement on the target machine: **Java 17 JRE** and **MySQL 8.x**.

---

## Dependency Summary

```xml
<!-- pom.xml key dependencies -->
com.formdev:flatlaf:3.4.1          <!-- Swing L&F -->
mysql:mysql-connector-java:8.0.33  <!-- JDBC driver -->
com.zaxxer:HikariCP:5.1.0          <!-- Connection pool -->
org.mindrot:jbcrypt:0.4            <!-- Password hashing -->
org.jfree:jfreechart:1.5.4         <!-- Charts -->
org.apache.pdfbox:pdfbox:2.0.30    <!-- PDF invoices -->
com.google.zxing:core:3.5.2        <!-- QR codes -->
org.slf4j:slf4j-api:2.0.9          <!-- Logging API -->
org.slf4j:slf4j-simple:2.0.9       <!-- Logging backend -->
```
