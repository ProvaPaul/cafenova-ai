# Installation Guide

## Prerequisites

| Requirement | Version | Notes |
|---|---|---|
| Java JDK | 17 or later | Oracle JDK or OpenJDK |
| Apache Maven | 3.8+ | Or use the wrapper inside NetBeans |
| XAMPP | Latest | MySQL + Apache (MySQL must be on port 3307) |
| NetBeans IDE | 15+ | Optional — any Java IDE works |
| Git | Any | For cloning the repository |

---

## Step 1 — Install Java 17

1. Download from https://www.oracle.com/java/technologies/downloads/ (or use OpenJDK)
2. Run the installer
3. Verify: open a terminal and run `java -version` — should show `17.x.x`
4. Set `JAVA_HOME` to the JDK folder (e.g. `C:\Program Files\Java\jdk-17`)

---

## Step 2 — Install XAMPP

1. Download XAMPP from https://www.apachefriends.org/
2. Install to `C:\xampp` (recommended)
3. Open XAMPP Control Panel
4. Click **Config** next to MySQL → edit `my.ini`
5. Change `port=3306` to `port=3307`
6. Click **Start** next to MySQL
7. Verify MySQL is running (green status in XAMPP Control Panel)

---

## Step 3 — Install Apache Maven

1. Download from https://maven.apache.org/download.cgi
2. Extract to `C:\maven`
3. Add `C:\maven\bin` to your system `PATH`
4. Verify: `mvn -version` in a new terminal

Alternatively, NetBeans 15 ships with Maven at:
`C:\Program Files\NetBeans-15\netbeans\java\maven\bin\mvn.cmd`

---

## Step 4 — Clone the Repository

```bash
git clone https://github.com/your-username/smart-cafe-management.git
cd "smart-cafe-management"
```

Or download the ZIP from GitHub and extract it.

---

## Step 5 — Set Up the Database

See [DATABASE_SETUP.md](DATABASE_SETUP.md) for the full guide.

**Quick version:**
```bash
# Start MySQL via XAMPP, then:
C:\xampp\mysql\bin\mysql.exe -u root -P 3307 -e "CREATE DATABASE smart_cafe_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
C:\xampp\mysql\bin\mysql.exe -u root -P 3307 smart_cafe_db < "src\main\resources\db\schema.sql"
```

---

## Step 6 — Configure the Database Connection

Edit `src/main/resources/config/database.properties`:

```properties
db.url=jdbc:mysql://localhost:3307/smart_cafe_db?useSSL=false&serverTimezone=Asia/Manila
db.username=root
db.password=
db.pool.maximumPoolSize=10
db.pool.minimumIdle=2
db.pool.connectionTimeout=30000
db.pool.idleTimeout=600000
db.pool.maxLifetime=1800000
```

If your MySQL has a password, set `db.password=your_password`.

---

## Step 7 — Build the Application

```bash
mvn clean package -DskipTests
```

This produces:
- `target/smart-cafe-management-jar-with-dependencies.jar` — fat JAR with all dependencies bundled

---

## Step 8 — Run the Application

```bash
java -jar target/smart-cafe-management-jar-with-dependencies.jar
```

Or on Windows, double-click the JAR (if Java is associated with `.jar` files).

---

## Opening in NetBeans

1. Open NetBeans 15
2. **File → Open Project** → select the project folder
3. Right-click project → **Build**
4. Right-click project → **Run**

---

## Troubleshooting

| Problem | Solution |
|---|---|
| `Cannot connect to the database` | Make sure XAMPP MySQL is running on port 3307 |
| `BUILD FAILURE — compiler error` | Ensure `JAVA_HOME` points to JDK 17 (not JRE) |
| App launches but login fails | Check that seed users were inserted (see DATABASE_SETUP.md) |
| Dark mode looks wrong | Update your display driver; FlatLaf needs DirectX/Metal support |
| PDF invoice fails to open | Check that a default PDF viewer is installed |
