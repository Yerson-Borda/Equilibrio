from pydantic import BaseModel
from datetime import datetime, date
from decimal import Decimal

class BudgetBase(BaseModel):
    monthly_limit: Decimal = Decimal("0.00")
    daily_limit: Decimal = Decimal("0.00")

class BudgetCreate(BudgetBase):
    pass

class BudgetUpdate(BaseModel):
    monthly_limit: Decimal | None = None
    daily_limit: Decimal | None = None

class BudgetResponse(BudgetBase):
    id: int
    month: int
    year: int
    monthly_spent: Decimal
    daily_spent: Decimal
    last_updated_date: date
    created_at: datetime

    class Config:
        from_attributes = True