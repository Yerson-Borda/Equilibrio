from datetime import datetime, date, timedelta
from decimal import ROUND_HALF_UP, Decimal
from typing import List, Dict, Any
from fastapi import HTTPException
from sqlalchemy import func
from sqlalchemy.orm import Session

from app.entities.transaction import Transaction
from app.entities.category import Category
from app.entities.user import User
from app.entities.monthly_savings_goal import MonthlySavingsGoal as SavingsGoal
from app.utils.enums.transaction_type import TransactionType

from .model import (
    CategorySummary,
    PeriodSummary,
    MonthlyComparison,
    DateRange
)

def get_category_summary_service(
        db: Session,
        current_user: User,
        start_date: date,
        end_date: date,
) -> PeriodSummary:
    """Get expenses and incomes by category for selected period."""
    try:
        # Validate date range
        DateRange.validate_range(start_date, end_date)

        # Query expenses by category
        expense_results = (
            db.query(
                Category.id,
                Category.name,
                Category.type,
                func.sum(Transaction.amount).label("total_amount"),
                func.count(Transaction.id).label("transaction_count"),
            )
            .join(Transaction, Transaction.category_id == Category.id)
            .filter(
                Transaction.user_id == current_user.id,
                Transaction.type == TransactionType.EXPENSE,
                Transaction.transaction_date >= start_date,
                Transaction.transaction_date <= end_date
            )
            .group_by(Category.id, Category.name, Category.type)
            .all()
        )

        # Query incomes by category
        income_results = (
            db.query(
                Category.id,
                Category.name,
                Category.type,
                func.sum(Transaction.amount).label("total_amount"),
                func.count(Transaction.id).label("transaction_count"),
            )
            .join(Transaction, Transaction.category_id == Category.id)
            .filter(
                Transaction.user_id == current_user.id,
                Transaction.type == TransactionType.INCOME,
                Transaction.transaction_date >= start_date,
                Transaction.transaction_date <= end_date
            )
            .group_by(Category.id, Category.name, Category.type)
            .all()
        )

        # Calculate totals
        total_expenses = sum(float(result.total_amount or 0) for result in expense_results)
        total_incomes = sum(float(result.total_amount or 0) for result in income_results)

        return PeriodSummary(
            expenses=[
                CategorySummary(
                    category_id=result.id,
                    category_name=result.name,
                    category_type=result.type.value,
                    total_amount=float(result.total_amount or 0),
                    transaction_count=result.transaction_count,
                )
                for result in expense_results
            ],
            incomes=[
                CategorySummary(
                    category_id=result.id,
                    category_name=result.name,
                    category_type=result.type.value,
                    total_amount=float(result.total_amount or 0),
                    transaction_count=result.transaction_count,
                )
                for result in income_results
            ],
            total_expenses=total_expenses,
            total_incomes=total_incomes,
            net_flow=total_incomes - total_expenses,
        )

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Internal server error while generating category summary: {str(e)}",
        )


def get_monthly_comparison_service(
        db: Session,
        current_user: User,
        month: str,
) -> List[MonthlyComparison]:
    """
    Get category-wise comparison between selected month and previous month
    (automatically calculated).
    """
    try:
        # Validate and parse selected month
        try:
            selected_date = datetime.strptime(month, "%Y-%m").date()
        except ValueError:
            raise HTTPException(
                status_code=400,
                detail="Invalid date format. Use YYYY-MM format (e.g., 2024-01)",
            )

        # Calculate previous month (month - 1) automatically
        if selected_date.month == 1:
            # If selected month is January, previous month is December of previous year
            previous_date = selected_date.replace(year=selected_date.year - 1, month=12)
        else:
            previous_date = selected_date.replace(month=selected_date.month - 1)

        # Calculate date ranges for both months
        def get_month_range(base_date: date):
            start_date = base_date.replace(day=1)
            if base_date.month == 12:
                end_date = base_date.replace(
                    year=base_date.year + 1, month=1, day=1
                ) - timedelta(days=1)
            else:
                end_date = base_date.replace(month=base_date.month + 1, day=1) - timedelta(
                    days=1
                )
            return start_date, end_date

        selected_start, selected_end = get_month_range(selected_date)
        previous_start, previous_end = get_month_range(previous_date)

        # Get selected month data (both income and expense)
        selected_month_data = (
            db.query(
                Category.id,
                Category.name,
                Transaction.type,
                func.sum(Transaction.amount).label("total_amount"),
            )
            .join(Transaction, Transaction.category_id == Category.id)
            .filter(
                Transaction.user_id == current_user.id,
                Transaction.transaction_date >= selected_start,
                Transaction.transaction_date <= selected_end,
            )
            .group_by(Category.id, Category.name, Transaction.type)
            .all()
        )

        # Get previous month data (both income and expense)
        previous_month_data = (
            db.query(
                Category.id,
                Category.name,
                Transaction.type,
                func.sum(Transaction.amount).label("total_amount"),
            )
            .join(Transaction, Transaction.category_id == Category.id)
            .filter(
                Transaction.user_id == current_user.id,
                Transaction.transaction_date >= previous_start,
                Transaction.transaction_date <= previous_end,
            )
            .group_by(Category.id, Category.name, Transaction.type)
            .all()
        )

        # Create dictionaries for easy lookup (separate by type)
        selected_dict: Dict[Any, Any] = {}
        for result in selected_month_data:
            key = (result.id, result.type)
            selected_dict[key] = (result.name, float(result.total_amount or 0))

        previous_dict: Dict[Any, Any] = {}
        for result in previous_month_data:
            key = (result.id, result.type)
            previous_dict[key] = (result.name, float(result.total_amount or 0))

        # Combine all categories from both months and both types
        all_category_keys = set(selected_dict.keys()) | set(previous_dict.keys())

        comparisons: List[MonthlyComparison] = []
        for category_key in all_category_keys:
            category_id, transaction_type = category_key
            selected_name, selected_amount = selected_dict.get(category_key, ("Unknown", 0))
            previous_name, previous_amount = previous_dict.get(category_key, ("Unknown", 0))

            # Use the name from whichever record exists
            category_name = selected_name if selected_name != "Unknown" else previous_name

            # Calculate difference and percentage change
            difference = selected_amount - previous_amount

            # Handle percentage change calculation safely
            if previous_amount != 0:
                percentage_change = (difference / previous_amount) * 100
            else:
                # If previous amount was 0, any current amount is 100% increase
                percentage_change = 100 if selected_amount > 0 else 0

            comparisons.append(
                MonthlyComparison(
                    category_id=category_id,
                    category_name=f"{category_name} ({transaction_type.value})",
                    current_month_amount=round(selected_amount, 2),
                    previous_month_amount=round(previous_amount, 2),
                    difference=round(difference, 2),
                    percentage_change=round(percentage_change, 2),
                )
            )

        # Sort by absolute difference (biggest changes first)
        comparisons.sort(key=lambda x: abs(x.difference), reverse=True)

        return comparisons

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Internal server error while generating monthly comparison: {str(e)}",
        )


def get_spending_trends_service(
        db: Session,
        current_user: User,
        months: int,
) -> dict:
    """Get spending trends over multiple months."""
    from sqlalchemy import extract

    try:
        if months < 1 or months > 12:
            raise HTTPException(
                status_code=400,
                detail="Months parameter must be between 1 and 12",
            )

        end_date = date.today()
        start_date = end_date - timedelta(days=30 * months)

        # Query monthly spending for both income and expense
        monthly_data = (
            db.query(
                func.extract("year", Transaction.transaction_date).label("year"),
                func.extract("month", Transaction.transaction_date).label("month"),
                Transaction.type,
                func.sum(Transaction.amount).label("total_amount"),
            )
            .filter(
                Transaction.user_id == current_user.id,
                Transaction.transaction_date >= start_date,
                Transaction.transaction_date <= end_date,
                Transaction.type.in_([TransactionType.INCOME, TransactionType.EXPENSE]),
                )
            .group_by("year", "month", Transaction.type)
            .order_by("year", "month")
            .all()
        )

        # Organize data by month
        monthly_totals: Dict[str, Dict[str, Any]] = {}
        for result in monthly_data:
            key = f"{int(result.year)}-{int(result.month):02d}"
            if key not in monthly_totals:
                monthly_totals[key] = {
                    "year": int(result.year),
                    "month": int(result.month),
                    "total_spent": 0.0,
                    "total_income": 0.0,
                    "month_name": f"{int(result.year)}-{int(result.month):02d}",
                    "display_name": datetime(
                        int(result.year), int(result.month), 1
                    ).strftime("%b %Y"),
                }

            if result.type == TransactionType.EXPENSE:
                monthly_totals[key]["total_spent"] = float(result.total_amount or 0)
            elif result.type == TransactionType.INCOME:
                monthly_totals[key]["total_income"] = float(result.total_amount or 0)

        # Fill in missing months with zero values
        complete_monthly_data = []
        current = start_date.replace(day=1)
        while current <= end_date.replace(day=1):
            month_key = f"{current.year}-{current.month:02d}"
            existing_month = monthly_totals.get(
                month_key,
                {
                    "year": current.year,
                    "month": current.month,
                    "total_spent": 0.0,
                    "total_income": 0.0,
                    "month_name": month_key,
                    "display_name": current.strftime("%b %Y"),
                },
            )
            complete_monthly_data.append(existing_month)

            # Move to next month
            if current.month == 12:
                current = current.replace(year=current.year + 1, month=1)
            else:
                current = current.replace(month=current.month + 1)

        # Calculate summary statistics
        total_spent = sum(month["total_spent"] for month in complete_monthly_data)
        total_income = sum(month["total_income"] for month in complete_monthly_data)
        average_monthly_spent = (
            total_spent / len(complete_monthly_data) if complete_monthly_data else 0
        )

        return {
            "monthly_summary": complete_monthly_data,
            "summary": {
                "total_spent": round(total_spent, 2),
                "total_income": round(total_income, 2),
                "net_flow": round(total_income - total_spent, 2),
                "average_monthly_spent": round(average_monthly_spent, 2),
                "months_analyzed": len(complete_monthly_data),
            },
            "analysis_period": {
                "start_date": start_date.isoformat(),
                "end_date": end_date.isoformat(),
                "months_analyzed": months,
            },
        }

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Internal server error while generating spending trends: {str(e)}",
        )
    

def get_top_categories_current_month_service(db: Session, current_user: User):
    today = date.today()
    start_date = today.replace(day=1)

    # Compute end of month
    if today.month == 12:
        end_date = date(today.year, 12, 31)
    else:
        end_date = date(today.year, today.month + 1, 1) - timedelta(days=1)

    results = (
        db.query(
            Category.id,
            Category.name,
            func.sum(Transaction.amount).label("total_amount")
        )
        .join(Transaction, Transaction.category_id == Category.id)
        .filter(
            Transaction.user_id == current_user.id,
            Transaction.type == TransactionType.EXPENSE,
            Transaction.transaction_date >= start_date,
            Transaction.transaction_date <= end_date,
        )
        .group_by(Category.id, Category.name)
        .order_by(func.sum(Transaction.amount).desc())
        .limit(3)
        .all()
    )

    return [
        {
            "category_id": r.id,
            "category_name": r.name,
            "total_amount": float(r.total_amount or 0),
        }
        for r in results
    ]


def get_average_spending_service(db: Session, current_user: User, period: str):
    try:
        if period not in ("year", "month", "day"):
            raise HTTPException(status_code=400, detail="Invalid period. Use 'year', 'month' or 'day'")

        today = date.today()
        current_year = today.year

        # get total spent per category for the current calendar year
        results = (
            db.query(
                Category.id.label("category_id"),
                Category.name.label("category_name"),
                func.coalesce(func.sum(Transaction.amount), 0).label("total_year_spent"),
                func.count(Transaction.id).label("txn_count")
            )
            .join(Transaction, Transaction.category_id == Category.id)
            .filter(
                Transaction.user_id == current_user.id,
                Transaction.type == TransactionType.EXPENSE,
                func.extract("year", Transaction.transaction_date) == current_year,
            )
            .group_by(Category.id, Category.name)
            .all()
        )

        response = []
        for r in results:
            total_year_spent = Decimal(str(r.total_year_spent or 0))

            # monthly average = total_year / 12
            monthly_avg = (total_year_spent / Decimal("12")) if total_year_spent else Decimal("0.00")

            # daily average = monthly_avg / 30
            daily_avg = (monthly_avg / Decimal("30")) if monthly_avg else Decimal("0.00")

            if period == "year":
                item = {
                    "category_id": int(r.category_id),
                    "category_name": r.category_name,
                    "total_period_spent": _to_float_round(total_year_spent, 2),
                    "transactions": int(r.txn_count),
                    "period_type": "year"
                }
            elif period == "month":
                item = {
                    "category_id": int(r.category_id),
                    "category_name": r.category_name,
                    "average_monthly_spending": _to_float_round(monthly_avg, 2),
                    "total_period_spent": _to_float_round(total_year_spent, 2),
                    "transactions": int(r.txn_count),
                    "period_type": "month"
                }
            else:
                item = {
                    "category_id": int(r.category_id),
                    "category_name": r.category_name,
                    "average_daily_spending": _to_float_round(daily_avg, 2),
                    "average_monthly_spending": _to_float_round(monthly_avg, 2),
                    "total_period_spent": _to_float_round(total_year_spent, 2),
                    "transactions": int(r.txn_count),
                    "period_type": "day"
                }

            response.append(item)

        return response

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Internal server error: {str(e)}")


def get_savings_trends_service(
    db: Session,
    current_user: User,
    months: int,
):
    end_date = date.today()
    start_date = end_date - timedelta(days=30 * months)

    start_year = start_date.year
    start_month = start_date.month

    data = (
        db.query(
            SavingsGoal.year,
            SavingsGoal.month,
            SavingsGoal.current_saved,
            SavingsGoal.target_amount,
        )
        .filter(
            SavingsGoal.user_id == current_user.id,
            (
                (SavingsGoal.year > start_year) |
                (
                    (SavingsGoal.year == start_year) &
                    (SavingsGoal.month >= start_month)
                )
            )
        )
        .order_by(SavingsGoal.year, SavingsGoal.month)
        .all()
    )

    indexed = {
        (row.year, row.month): row
        for row in data
    }

    monthly_trends = []

    current = start_date.replace(day=1)
    end_month = end_date.replace(day=1)

    while current <= end_month:
        row = indexed.get((current.year, current.month))

        saved = float(row.current_saved) if row else 0.0
        target = float(row.target_amount) if row else 0.0

        achievement = (
            round(saved / target, 2)
            if target > 0
            else 0.0
        )

        monthly_trends.append({
            "year": current.year,
            "month": current.month,
            "display_name": current.strftime("%b %Y"),
            "saved_amount": saved,
            "target_amount": target,
            "achievement_rate": achievement,
        })

        if current.month == 12:
            current = current.replace(year=current.year + 1, month=1)
        else:
            current = current.replace(month=current.month + 1)

    return {
        "monthly_trends": monthly_trends,
        "months_analyzed": months,
        "analysis_period": {
            "start_date": start_date.isoformat(),
            "end_date": end_date.isoformat(),
        },
    }



def _to_float_round(value: Decimal | None, places: int = 2) -> float:
    if value is None:
        return 0.0
    return float(Decimal(value).quantize(Decimal(f"1.{'0'*places}"), rounding=ROUND_HALF_UP))