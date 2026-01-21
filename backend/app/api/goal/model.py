# app/api/goals/model.py
from pydantic import BaseModel
from datetime import date
from decimal import Decimal

class GoalCreate(BaseModel):
    title: str
    description: str | None = None
    deadline: date | None = None
    goal_amount: Decimal
    currency: str = "USD"

class GoalUpdate(BaseModel):
    title: str | None = None
    description: str | None = None
    image: str | None = None
    deadline: date | None = None
    goal_amount: Decimal | None = None

class GoalResponse(BaseModel):
    id: int
    title: str
    description: str | None
    image: str | None
    deadline: date | None
    goal_amount: Decimal
    amount_saved: Decimal
    wallet_id: int
    currency: str

    class Config:
        from_attributes = True
