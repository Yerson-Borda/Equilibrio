from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session
from app.api.category_limits.model import CategoryLimitOverviewItem, CategoryLimitResponse, CategoryLimitUpdate
from app.api.category_limits.service import CategoryLimitService
from app.database import get_db
from app.entities.user import User
from app.core.auth import get_current_user

router = APIRouter()

@router.put("/{category_id}", response_model=CategoryLimitResponse)
def set_limit(
    category_id: int,
    data: CategoryLimitUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    return CategoryLimitService.set_limit(db, current_user.id, category_id, data.monthly_limit)


@router.delete("/{category_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_limit(
    category_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    CategoryLimitService.delete_limit(db, current_user.id, category_id)
    return


@router.get("/", response_model=list[CategoryLimitOverviewItem])
def get_limits_overview(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    return CategoryLimitService.get_all_category_limits_with_spent(db, current_user.id)
