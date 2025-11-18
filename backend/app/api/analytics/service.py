from datetime import datetime, date, timedelta
from typing import List, Dict, Any
from fastapi import HTTPException
from sqlalchemy import func
from sqlalchemy.orm import Session
from app.database import get_db  # noqa: F401  (kept if you want DI elsewhere)

from app.entities.transaction import Transaction
from app.entities.category import Category
from app.entities.user import User


from .model import (
    CategorySummary,
    PeriodSummary,
    MonthlyComparison,
    DateRange,
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
                Transaction.transaction_date <= end_date,
                Transaction.is_deleted == False,  # noqa: E712
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
                Transaction.transaction_date <= end_date,
                Transaction.is_deleted == False,  # noqa: E712
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
        print(f"❌ Error in category summary: {str(e)}")
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

        print(f"🔍 Monthly comparison: Selected={selected_date}, Previous={previous_date}")

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

        print(f"📅 Date ranges - Selected: {selected_start} to {selected_end}")
        print(f"📅 Date ranges - Previous: {previous_start} to {previous_end}")

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
                Transaction.is_deleted == False,  # noqa: E712
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
                Transaction.is_deleted == False,  # noqa: E712
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

        print(f"📊 Total unique categories with types: {len(all_category_keys)}")

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

        print(f"✅ Monthly comparison completed: {len(comparisons)} categories compared")

        return comparisons

    except HTTPException:
        raise
    except Exception as e:
        print(f"❌ Error in monthly comparison: {str(e)}")
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
                Transaction.is_deleted == False,  # noqa: E712
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
        print(f"❌ Error in spending trends: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"Internal server error while generating spending trends: {str(e)}",
        )
