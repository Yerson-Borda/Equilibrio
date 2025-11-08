from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app.models.models import Category, TransactionType
from app.schemas.schemas import CategoryCreate, CategoryResponse
from app.auth import get_current_user

router = APIRouter()

@router.get("/", response_model=List[CategoryResponse])
def get_categories(
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Get all categories (both system and user's custom categories)"""
    categories = db.query(Category).filter(
        (Category.user_id == current_user.id) | (Category.user_id.is_(None))
    ).all()
    
    return categories

@router.get("/income", response_model=List[CategoryResponse])
def get_income_categories(
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Get only income categories"""
    categories = db.query(Category).filter(
        ((Category.user_id == current_user.id) | (Category.user_id.is_(None))) &
        (Category.type == TransactionType.INCOME)
    ).all()
    
    return categories

@router.get("/expense", response_model=List[CategoryResponse])
def get_expense_categories(
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Get only expense categories"""
    categories = db.query(Category).filter(
        ((Category.user_id == current_user.id) | (Category.user_id.is_(None))) &
        (Category.type == TransactionType.EXPENSE)
    ).all()
    
    return categories

@router.post("/", response_model=CategoryResponse, status_code=status.HTTP_201_CREATED)
def create_category(
    category_data: CategoryCreate,
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Create a new custom category"""
    # Check if category with same name and type already exists for this user
    existing_category = db.query(Category).filter(
        Category.user_id == current_user.id,
        Category.name == category_data.name,
        Category.type == category_data.type
    ).first()
    
    if existing_category:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Category with name '{category_data.name}' and type '{category_data.type.value}' already exists"
        )
    
    category = Category(
        name=category_data.name,
        type=category_data.type,
        color=category_data.color,
        icon=category_data.icon,
        user_id=current_user.id  # Set user_id to mark as custom category
    )
    
    db.add(category)
    db.commit()
    db.refresh(category)
    
    return category

@router.delete("/{category_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_category(
    category_id: int,
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Delete a custom category (only user's own categories)"""
    category = db.query(Category).filter(
        Category.id == category_id,
        Category.user_id == current_user.id
    ).first()
    
    if not category:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Category not found or you don't have permission to delete it"
        )
    
    db.delete(category)
    db.commit()
    
    return None