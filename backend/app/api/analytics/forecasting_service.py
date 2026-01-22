from datetime import date
from sqlalchemy.orm import Session
from typing import Dict, Any

from app.entities.user import User
from datetime import timedelta
from sqlalchemy import func

from app.entities.transaction import Transaction
from app.utils.enums.transaction_type import TransactionType
from app.api.analytics.service import (
    get_average_spending_service,
    get_spending_trends_service,
)
from app.entities.wallet import Wallet
from app.utils.enums.wallet_type import WalletType


'''
This function forecasts future savings progress by calculating the users average monthly saving rate from past months and projecting it forward.

It assumes:
- The user saves consistently
- Past behavior is the best predictor of short-term future behavior

* This is a trend-based projection, not a prediction.
'''

def get_savings_forecast_service(
    db: Session,
    current_user: User,
    months_ahead: int = 3,
) -> Dict[str, Any]:
    """
    Forecast future savings based on money accumulated in saving-related wallets.

    Counts all transactions (income or transfers) that increase the balance
    of SAVING_ACCOUNT or GOAL wallets.
    """

    monthly_savings = (
        db.query(
            func.extract("year", Transaction.transaction_date).label("year"),
            func.extract("month", Transaction.transaction_date).label("month"),
            func.sum(Transaction.amount).label("saved_amount"),
        )
        .join(Wallet, Transaction.wallet_id == Wallet.id)
        .filter(
            Transaction.user_id == current_user.id,
            Wallet.wallet_type.in_([
                WalletType.SAVING_ACCOUNT,
                WalletType.GOAL,
            ]),
        )
        .group_by("year", "month")
        .order_by("year", "month")
        .all()
    )

    if len(monthly_savings) < 2:
        return {
            "average_monthly_saving": 0.0,
            "forecast": [],
            "note": "Not enough historical saving data to generate forecast",
        }

    monthly_amounts = [float(row.saved_amount or 0) for row in monthly_savings]
    average_monthly_saving = sum(monthly_amounts) / len(monthly_amounts)

    last_month_amount = monthly_amounts[-1]

    forecast = []
    for i in range(1, months_ahead + 1):
        forecast.append({
            "month_offset": i,
            "predicted_saved_amount": round(
                last_month_amount + average_monthly_saving * i, 2
            ),
        })

    return {
        "average_monthly_saving": round(average_monthly_saving, 2),
        "forecast": forecast,
        "months_ahead": months_ahead,
        "based_on_months": len(monthly_amounts),
    }


'''
This function estimates total spending at the end of the current month using:
- Spending so far
- Number of days elapsed
- Total days in the month

* It assumes spending continues at the current daily average rate.
'''
def get_end_of_month_spending_forecast_service(
    db: Session,
    current_user: User,
) -> Dict[str, Any]:
    """
    Forecast the user's total spending for the current month
    using linear projection based on daily average spending.
    """

    today = date.today()
    start_date = today.replace(day=1)

    spent_so_far = (
        db.query(func.coalesce(func.sum(Transaction.amount), 0))
        .filter(
            Transaction.user_id == current_user.id,
            Transaction.type == TransactionType.EXPENSE,
            Transaction.transaction_date >= start_date,
            Transaction.transaction_date <= today,
        )
        .scalar()
    )

    days_elapsed = max(today.day, 1)

    # Compute days in month
    if today.month == 12:
        days_in_month = 31
    else:
        days_in_month = (
            today.replace(month=today.month + 1, day=1) - timedelta(days=1)
        ).day

    daily_average = float(spent_so_far) / days_elapsed
    forecast_total = daily_average * days_in_month

    return {
        "spent_so_far": round(float(spent_so_far), 2),
        "daily_average_spending": round(daily_average, 2),
        "forecast_end_of_month": round(forecast_total, 2),
        "days_elapsed": days_elapsed,
        "days_in_month": days_in_month,
        "confidence": "medium",
    }


'''
This function generates rule-based saving recommendations by comparing:
- Current month spending
- Historical averages
- Net monthly cash flow

* It produces actionable insights, not predictions.
'''
def get_saving_opportunities_service(
    db: Session,
    current_user: User,
) -> Dict[str, Any]:
    """
    Suggest saving opportunities based on spending patterns and trends.
    """

    suggestions = []

    # 1. Overspending compared to historical average
    averages = get_average_spending_service(db, current_user, period="month")
    current_trends = get_spending_trends_service(db, current_user, months=1)

    current_month = current_trends["monthly_summary"][-1]

    for avg in averages:
        if avg["average_monthly_spending"] > 0:
            ratio = (
                current_month["total_spent"]
                / avg["average_monthly_spending"]
            )

            if ratio > 1.3:
                suggestions.append({
                    "type": "overspending",
                    "category": avg["category_name"],
                    "message": (
                        f"You are spending significantly more than usual "
                        f"in {avg['category_name']}."
                    ),
                })

    # 2. Positive cash flow but no explicit saving
    if current_month["total_income"] > current_month["total_spent"]:
        suggestions.append({
            "type": "saving_potential",
            "message": (
                "You have a positive cash flow this month. "
                "Consider allocating the surplus to a savings goal."
            ),
        })

    return {
        "suggestions": suggestions,
        "generated_at": date.today().isoformat(),
    }