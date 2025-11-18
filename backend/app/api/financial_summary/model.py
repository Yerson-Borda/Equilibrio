from pydantic import BaseModel
from datetime import datetime
from decimal import Decimal

class FinancialSummaryBase(BaseModel):
    month: int
    year: int
    total_income: Decimal
    total_spent: Decimal
    total_saved: Decimal

class FinancialSummaryResponse(FinancialSummaryBase):
    id: int
    created_at: datetime

    class Config:
        from_attributes = True