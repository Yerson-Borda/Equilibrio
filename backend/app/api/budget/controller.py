from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.database import get_db
from app.entities.user import User
from app.api.budget.model import BudgetResponse, BudgetUpdate
from app.core.auth import get_current_user
from app.api.budget import service as budget_service

router = APIRouter()

@router.get("/current", response_model=BudgetResponse)
def get_current_budget(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    budget = budget_service.refresh_budget_if_needed(db, current_user.id)
    return budget
    

@router.put("/current", response_model=BudgetResponse)
def update_current_budget(
    data: BudgetUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    budget = budget_service.get_or_create_current_budget(db, current_user.id)
    updated_budget = budget_service.update_budget(db, budget, data)
    return updated_budget