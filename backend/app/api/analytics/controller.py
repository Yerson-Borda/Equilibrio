from datetime import date
from typing import List
from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from app.database import get_db
from app.entities.user import User

from .model import PeriodSummary, MonthlyComparison
from .service import (
    get_category_summary_service,
    get_monthly_comparison_service,
    get_spending_trends_service,
    get_top_categories_current_month_service,
    get_average_spending_service,
    get_savings_trends_service
)
from ...core.auth import get_current_user

router = APIRouter()


@router.get("/category-summary", response_model=PeriodSummary)
def get_category_summary(
        start_date: date = Query(..., description="Start date (YYYY-MM-DD)"),
        end_date: date = Query(..., description="End date (YYYY-MM-DD)"),
        current_user: User = Depends(get_current_user),
        db: Session = Depends(get_db),
):
    """
    Get expenses and incomes by category for selected period.
    Delegates logic to the analytics service.
    """
    return get_category_summary_service(
        db=db,
        current_user=current_user,
        start_date=start_date,
        end_date=end_date,
    )


@router.get("/monthly-comparison", response_model=List[MonthlyComparison])
def get_monthly_comparison(
        month: str = Query(
            ...,
            description=(
                    "Selected month in YYYY-MM format. "
                    "Will compare with previous month automatically"
            ),
        ),
        current_user: User = Depends(get_current_user),
        db: Session = Depends(get_db),
):
    """
    Get category-wise comparison between selected month and previous month (auto-calculated).
    """
    return get_monthly_comparison_service(
        db=db,
        current_user=current_user,
        month=month,
    )


@router.get("/spending-trends")
def get_spending_trends(
        months: int = Query(
            6,
            ge=1,
            le=12,
            description="Number of months to analyze (1-12)",
        ),
        current_user: User = Depends(get_current_user),
        db: Session = Depends(get_db),
):
    """
    Get spending trends over multiple months (income vs expense).
    """
    return get_spending_trends_service(
        db=db,
        current_user=current_user,
        months=months,
    )


@router.get("/top-categories/current-month")
def get_top_categories_current_month(
        current_user: User = Depends(get_current_user),
        db: Session = Depends(get_db),
):
    return get_top_categories_current_month_service(
        db=db,
        current_user=current_user,
    )


@router.get("/average-spending", response_model=list)
def get_average_spending(
        period: str = Query(..., enum=["day", "month", "year"]),
        current_user: User = Depends(get_current_user),
        db: Session = Depends(get_db),
):
    return get_average_spending_service(
        db=db,
        current_user=current_user,
        period=period,
    )

@router.get("/savings-trends")
def get_savings_trends(
    months: int = Query(6, ge=1, le=12),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """
    Get savings trends over multiple months.
    """
    return get_savings_trends_service(
        db=db,
        current_user=current_user,
        months=months,
    )
