from pydantic import BaseModel
from decimal import Decimal

class SavingsGoalUpdate(BaseModel):
    target_amount: Decimal

class SavingsGoalResponse(BaseModel):
    id: int
    month: int
    year: int
    target_amount: Decimal
    current_saved: Decimal

    class Config:
        from_attributes = True