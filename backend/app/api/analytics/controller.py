from datetime import date
from typing import List
from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from app.database import get_db
from app.entities.user import User
from app.auth import get_current_user

from .model import PeriodSummary, MonthlyComparison
from .service import (
    get_category_summary_service,
    get_monthly_comparison_service,
    get_spending_trends_service,
)

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
