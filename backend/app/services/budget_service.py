from sqlalchemy.orm import Session
from datetime import date
from app.models.models import Budget
from app.schemas.schemas import BudgetUpdate
from decimal import Decimal


def get_or_create_current_budget(db: Session, user_id: int) -> Budget:
    today = date.today()
    budget = db.query(Budget).filter_by(
        user_id=user_id,
        month=today.month,
        year=today.year
    ).first()

    if not budget:
        budget = Budget(
            user_id=user_id,
            month=today.month,
            year=today.year,
        )
        db.add(budget)
        db.commit()
        db.refresh(budget)

    return budget


def update_budget(db: Session, budget: Budget, updates: BudgetUpdate):
    for field, value in updates.dict(exclude_unset=True).items():
        setattr(budget, field, value)
    db.commit()
    db.refresh(budget)
    return budget


def record_expense(db: Session, user_id: int, amount: float):
    """Increment spent amounts when an expense transaction is added."""
    today = date.today()
    budget = get_or_create_current_budget(db, user_id)
    budget.daily_spent += to_decimal(amount)
    budget.monthly_spent += to_decimal(amount)
    db.commit()
    return budget

def to_decimal(value):
    if isinstance(value, Decimal):
        return value
    return Decimal(str(value))