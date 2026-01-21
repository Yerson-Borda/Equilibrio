from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.database import get_db
from app.entities.user import User
from app.core.auth import get_current_user
from app.api.goal.model import (
    GoalCreate, GoalUpdate, GoalResponse
)
from app.api.goal.service import (
    create_goal, get_goals, get_goal, update_goal, delete_goal
)
from app.entities.goal import Goal
from fastapi import Form, UploadFile, File
from datetime import date
from decimal import Decimal
from typing import Optional

router = APIRouter()

class GoalCreateForm:
    def __init__(
        self,
        title: str = Form(...),
        goal_amount: Decimal = Form(...),
        currency: str = Form("USD"),
        description: Optional[str] = Form(None),
        deadline: Optional[date] = Form(None),
    ):
        self.data = GoalCreate(
            title=title,
            description=description,
            deadline=deadline,
            goal_amount=goal_amount,
            currency=currency
        )

@router.post("", response_model=GoalResponse)
def create(
    form: GoalCreateForm = Depends(),
    image: UploadFile | None = File(None),
    db: Session = Depends(get_db),
    user: User = Depends(get_current_user),
):
    goal = create_goal(
        db=db,
        user=user,
        data=form.data,
        image=image
    )
    return goal_to_response(goal)


@router.get("", response_model=list[GoalResponse])
def list_goals(
    db: Session = Depends(get_db),
    user: User = Depends(get_current_user),
):
    goals = get_goals(db, user)
    return [goal_to_response(g) for g in goals]


@router.get("/{goal_id}", response_model=GoalResponse)
def get(
    goal_id: int,
    db: Session = Depends(get_db),
    user: User = Depends(get_current_user),
):
    goal = get_goal(db, user, goal_id)
    return goal_to_response(goal)


@router.put("/{goal_id}", response_model=GoalResponse)
def update(
    goal_id: int,
    data: GoalUpdate,
    db: Session = Depends(get_db),
    user: User = Depends(get_current_user),
):
    goal = update_goal(db, user, goal_id, data)
    return goal_to_response(goal)


@router.delete("/{goal_id}")
def delete(
    goal_id: int,
    db: Session = Depends(get_db),
    user: User = Depends(get_current_user),
):
    delete_goal(db, user, goal_id)
    return {"message": "Goal deleted successfully"}


def goal_to_response(goal: Goal) -> GoalResponse:
    return GoalResponse(
        id=goal.id,
        title=goal.title,
        description=goal.description,
        image=goal.image,
        deadline=goal.deadline,
        goal_amount=goal.goal_amount,
        amount_saved=goal.amount_saved,
        wallet_id=goal.wallet_id,
        currency=goal.wallet.currency,
    )