from sqlalchemy.orm import Session
from datetime import date
from app.entities.budget import Budget
from app.api.budget.model import BudgetUpdate
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
    budget = refresh_budget_if_needed(db, user_id)
    budget.daily_spent += to_decimal(amount)
    budget.monthly_spent += to_decimal(amount)
    db.commit()
    return budget

def to_decimal(value):
    if isinstance(value, Decimal):
        return value
    return Decimal(str(value))

def refresh_budget_if_needed(db: Session, user_id: int):
    """Resets daily/monthly spending if a new day/month has started."""
    today = date.today()
    budget = get_or_create_current_budget(db, user_id)

    if budget.last_updated_date != today:
        # Reset daily
        budget.daily_spent = to_decimal("0.00")
        
        # Reset monthly if month or year changed
        if budget.last_updated_date.month != today.month or budget.last_updated_date.year != today.year:
            budget.monthly_spent = to_decimal("0.00")
        
        # Update tracker
        budget.last_updated_date = today
        
        db.commit()
        db.refresh(budget)

    return budget