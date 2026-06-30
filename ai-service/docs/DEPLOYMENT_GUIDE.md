# SmartCafe AI Service — Deployment Guide

## Requirements

| Component      | Version     | Notes                              |
|----------------|-------------|------------------------------------|
| Python         | 3.11+       |                                    |
| MySQL          | 8.x         | Port 3307 (XAMPP default)          |
| Java           | 17+         | Desktop app                        |
| Node.js        | 18+         | Customer website                   |
| XAMPP          | Any         | Apache + MySQL                     |

---

## First-Time Setup

### 1. Database

Start XAMPP. Open phpMyAdmin or MySQL shell:

```sql
source src/main/resources/db/schema.sql;
source src/main/resources/db/upgrade_v2.sql;
source src/main/resources/db/upgrade_v3.sql;
source src/main/resources/db/seed_data.sql;
```

### 2. AI Service

```bash
cd ai-service
pip install -r requirements.txt

# Verify DB connection
python -c "from app.db.connector import test_connection; print(test_connection())"
```

Configure `.env` if your MySQL credentials differ:
```
DB_HOST=localhost
DB_PORT=3307
DB_NAME=smart_cafe_db
DB_USER=root
DB_PASSWORD=
```

### 3. Start FastAPI

```bash
cd ai-service
python run.py
# or: uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

Open `http://localhost:8000/docs` — you should see the API documentation.

### 4. Generate Demo Data + Train AI

Option A — Admin Dashboard (recommended):
1. Open Java Desktop App → Admin → AI Analytics
2. Click **"Generate Demo Dataset"** (inserts 200 customers, 100 products, 2500 orders)
3. Click **"Retrain AI"** (trains Apriori, saves model)
4. Click **"Refresh"** to see model stats

Option B — Command line:
```bash
cd ai-service
python -m scripts.generate_demo_data
python -m scripts.train_from_db
```

### 5. Start Desktop App

```bash
mvn clean package -q
java -jar target/smart-cafe-management-jar-with-dependencies.jar
```

### 6. Start Customer Website

```bash
# Backend (Spring Boot)
cd customer-website/backend
mvn spring-boot:run

# Frontend (React)
cd customer-website/frontend
npm install
npm run dev
```

---

## Service Ports

| Service             | Port  |
|---------------------|-------|
| FastAPI (AI)        | 8000  |
| Spring Boot Backend | 8080  |
| React Frontend      | 5173  |
| MySQL               | 3307  |

---

## Retraining

### Manual Retrain (Admin Dashboard)
1. Admin → AI Analytics → **Retrain AI**

### Manual Retrain (CLI)
```bash
cd ai-service
python -m scripts.train_from_db
```

### Scheduled Retrain (Automatic)
Set `AUTO_RETRAIN_HOUR=3` in `.env` — retrain runs daily at 3 AM.
Set `AUTO_RETRAIN_HOUR=0` to disable.

---

## Transition to Real Customer Data

Once the cafe accumulates enough real orders:

1. Admin → AI Analytics → **Delete Demo Dataset**
2. Admin → AI Analytics → **Retrain AI**

The AI now learns from real customer purchases. No code changes needed.

---

## Troubleshooting

| Problem                         | Fix                                              |
|---------------------------------|--------------------------------------------------|
| `DB connection failed`          | Check XAMPP is running, port is 3307             |
| `0 rules after training`        | Need more COMPLETED orders; generate demo data   |
| `AI service unavailable`        | Start FastAPI with `python run.py`               |
| `Model not loaded`              | Run Retrain AI from Admin panel                  |
| `UnicodeError on Windows`       | Already fixed — all log messages use ASCII only  |
| `generate-demo: already exists` | Click Delete Demo Dataset first                  |
