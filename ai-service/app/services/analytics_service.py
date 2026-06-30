"""
Analytics Service — stub implementation for future AI-powered admin analytics.

All methods return realistic-looking dummy data.
Replace with real queries against the MySQL database when ready.
"""

from datetime import datetime, timedelta, timezone, date

from app.models.response import (
    AnalyticsDashboardResponse,
    AnalyticsMetric,
    SalesTrendResponse,
    SalesTrendPoint,
    PeakHoursResponse,
    PeakHourSlot,
    CustomerSegmentsResponse,
    CustomerSegment,
)


def _now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


def get_dashboard_analytics() -> AnalyticsDashboardResponse:
    return AnalyticsDashboardResponse(
        period="last_30_days",
        metrics=[
            AnalyticsMetric(label="Predicted Revenue (Next 7 days)", value="₱ 38,420",  change=12.4, trend="up"),
            AnalyticsMetric(label="Avg Order Value",                  value="₱ 485.50",  change=5.2,  trend="up"),
            AnalyticsMetric(label="Customer Retention Rate",          value="73%",        change=-2.1, trend="down"),
            AnalyticsMetric(label="Low-Stock Items",                  value=3,            change=None, trend="stable"),
            AnalyticsMetric(label="Top Category",                     value="Espresso Drinks", change=None, trend="up"),
            AnalyticsMetric(label="AI Confidence Score",              value="88%",        change=None, trend="stable"),
        ],
        insights=[
            "Caramel Macchiato sales peak on Tuesday and Thursday mornings — consider a weekday promotion.",
            "3 inventory items will reach minimum stock by Friday — restock Arabica Coffee Beans, Matcha Powder, and Oat Milk.",
            "Customer retention dropped 2.1% this month — 18 customers haven't visited in 30+ days.",
            "Weekend brunch orders (Sat 10am–1pm) are up 23% — consider adding a brunch bundle.",
            "Frappuccino sales correlate strongly with high-temperature days — plan iced-drink promotions.",
        ],
        generated_at=_now_iso(),
    )


def get_sales_trends(days: int = 14) -> SalesTrendResponse:
    import random
    rng = random.Random(99)
    today = date.today()
    trend = []
    base = 6500.0
    for i in range(days, 0, -1):
        d = today - timedelta(days=i)
        # Weekend bump
        bump = 1.3 if d.weekday() >= 5 else 1.0
        revenue = round(base * bump + rng.uniform(-800, 1200), 2)
        orders = rng.randint(28, 75)
        trend.append(SalesTrendPoint(
            date=d.isoformat(), revenue=revenue, order_count=orders, predicted=False,
        ))
    for j in range(1, 8):
        d = today + timedelta(days=j)
        bump = 1.3 if d.weekday() >= 5 else 1.0
        revenue = round(base * bump + rng.uniform(-400, 800), 2)
        orders = rng.randint(30, 65)
        trend.append(SalesTrendPoint(
            date=d.isoformat(), revenue=revenue, order_count=orders, predicted=True,
        ))
    return SalesTrendResponse(
        trend=trend,
        summary=(
            f"Revenue trending upward over the last {days} days. "
            "Next 7-day forecast shows ₱38,420 projected revenue (±12%)."
        ),
    )


def get_peak_hours() -> PeakHoursResponse:
    slots = [
        PeakHourSlot(hour="07:00–08:00", order_count=12, revenue=1820.00, is_peak=False),
        PeakHourSlot(hour="08:00–09:00", order_count=28, revenue=4210.00, is_peak=True),
        PeakHourSlot(hour="09:00–10:00", order_count=22, revenue=3350.00, is_peak=True),
        PeakHourSlot(hour="10:00–11:00", order_count=15, revenue=2280.00, is_peak=False),
        PeakHourSlot(hour="11:00–12:00", order_count=18, revenue=2750.00, is_peak=False),
        PeakHourSlot(hour="12:00–13:00", order_count=45, revenue=7890.00, is_peak=True),
        PeakHourSlot(hour="13:00–14:00", order_count=38, revenue=6620.00, is_peak=True),
        PeakHourSlot(hour="14:00–15:00", order_count=20, revenue=3100.00, is_peak=False),
        PeakHourSlot(hour="15:00–16:00", order_count=16, revenue=2450.00, is_peak=False),
        PeakHourSlot(hour="16:00–17:00", order_count=14, revenue=2100.00, is_peak=False),
        PeakHourSlot(hour="17:00–18:00", order_count=25, revenue=4100.00, is_peak=True),
        PeakHourSlot(hour="18:00–19:00", order_count=32, revenue=5480.00, is_peak=True),
        PeakHourSlot(hour="19:00–20:00", order_count=19, revenue=3200.00, is_peak=False),
        PeakHourSlot(hour="20:00–21:00", order_count=8,  revenue=1200.00, is_peak=False),
    ]
    return PeakHoursResponse(
        peak_hours=slots,
        busiest_hour="12:00–13:00",
        recommendation=(
            "Schedule maximum staff between 08:00–09:00, 12:00–14:00, and 17:00–19:00. "
            "Consider express counter during lunch peak to reduce wait times."
        ),
    )


def get_customer_segments() -> CustomerSegmentsResponse:
    segments = [
        CustomerSegment(
            segment="High-Value",
            count=8,
            description="Visit 4+ times/week, avg spend ₱680+. Champions of the cafe.",
            suggested_action="Exclusive loyalty tier upgrades and personalised thank-you offers.",
            avg_spend=720.00,
        ),
        CustomerSegment(
            segment="Loyal",
            count=15,
            description="Regular visitors, 2–3 times/week. Core customer base.",
            suggested_action="Reward with free item upgrade on 10th visit.",
            avg_spend=485.00,
        ),
        CustomerSegment(
            segment="Growing",
            count=12,
            description="Visit frequency increased in last 30 days. Momentum customers.",
            suggested_action="Send a 'Thank You' 10% discount to reinforce the habit.",
            avg_spend=380.00,
        ),
        CustomerSegment(
            segment="At-Risk",
            count=18,
            description="No visit in 30+ days but previously regular.",
            suggested_action="Send a win-back SMS: 'We miss you — here is a free drink coupon.'",
            avg_spend=320.00,
        ),
        CustomerSegment(
            segment="Churned",
            count=7,
            description="No visit in 90+ days.",
            suggested_action="Re-engagement campaign with a significant offer (20% off).",
            avg_spend=210.00,
        ),
    ]
    return CustomerSegmentsResponse(
        segments=segments,
        total_customers=sum(s.count for s in segments),
    )
