from datetime import date
from typing import List
from pydantic import BaseModel


class CategorySummary(BaseModel):
    category_id: int
    category_name: str
    category_type: str
    total_amount: float
    transaction_count: int


class PeriodSummary(BaseModel):
    expenses: List[CategorySummary]
    incomes: List[CategorySummary]
    total_expenses: float
    total_incomes: float
    net_flow: float


class MonthlyComparison(BaseModel):
    category_id: int
    category_name: str
    current_month_amount: float
    previous_month_amount: float
    difference: float
    percentage_change: float


class DateRange(BaseModel):
    start_date: date
    end_date: date

    @classmethod
    def validate_range(cls, start_date: date, end_date: date) -> "DateRange":
        from fastapi import HTTPException

        if start_date > end_date:
            raise HTTPException(
                status_code=400,
                detail="Start date cannot be after end date"
            )

        # Limit range to 1 year maximum
        if (end_date - start_date).days > 365:
            raise HTTPException(
                status_code=400,
                detail="Date range cannot exceed 1 year"
            )

        return cls(start_date=start_date, end_date=end_date)
