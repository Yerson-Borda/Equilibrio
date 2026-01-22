from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.database import get_db
from app.core.auth import get_current_user
from app.entities.user import User
from app.api.savings_goal import service
from app.api.savings_goal.model import (
    SavingsGoalResponse,
    SavingsGoalUpdate
)

router = APIRouter()

@router.get("/current", response_model=SavingsGoalResponse)
def get_current(
    db: Session = Depends(get_db),
    user: User = Depends(get_current_user)
):
    return service.get_or_create_current_savings_goal(db, user.id)

@router.put("/current", response_model=SavingsGoalResponse)
def update_target(
    data: SavingsGoalUpdate,
    db: Session = Depends(get_db),
    user: User = Depends(get_current_user)
):
    return service.update_savings_target(
        db=db,
        user_id=user.id,
        target_amount=data.target_amount
    )
