from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app.auth import get_current_user
from app.api.category.model import CategoryCreate, CategoryResponse
from app.dto.enums.transaction_type import TransactionType
from app.api.category.service import CategoryService

router = APIRouter()

@router.get("/", response_model=List[CategoryResponse])
def get_categories(current_user=Depends(get_current_user), db: Session = Depends(get_db)):
    return CategoryService.get_all_categories(db, current_user.id)


@router.get("/income", response_model=List[CategoryResponse])
def get_income_categories(current_user=Depends(get_current_user), db: Session = Depends(get_db)):
    return CategoryService.get_categories_by_type(db, current_user.id, TransactionType.INCOME)


@router.get("/expense", response_model=List[CategoryResponse])
def get_expense_categories(current_user=Depends(get_current_user), db: Session = Depends(get_db)):
    return CategoryService.get_categories_by_type(db, current_user.id, TransactionType.EXPENSE)


@router.post("/", response_model=CategoryResponse, status_code=status.HTTP_201_CREATED)
def create_category(
    category_data: CategoryCreate,
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    return CategoryService.create_category(db, current_user.id, category_data)


@router.delete("/{category_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_category(
    category_id: int, 
    current_user=Depends(get_current_user), 
    db: Session = Depends(get_db)
):
    CategoryService.delete_category(db, category_id, current_user.id)
    return None
