from fastapi import APIRouter, Depends, HTTPException, status, BackgroundTasks
from sqlalchemy.orm import Session
from typing import List
from datetime import datetime
from app.database import get_db
from app.models.models import Category, TransactionType
from app.schemas.schemas import CategoryCreate, CategoryResponse
from app.auth import get_current_user
from app.core.websocket_manager import manager
import json

router = APIRouter()

async def notify_user(user_id: int, event_type: str, data: dict):
    """Notify user about data changes via WebSocket"""
    message = {
        "event": event_type,
        "data": data,
        "timestamp": datetime.utcnow().isoformat()
    }
    await manager.send_personal_message(json.dumps(message), user_id)

@router.get("/", response_model=List[CategoryResponse])
def get_categories(
        current_user = Depends(get_current_user),
        db: Session = Depends(get_db)
):
    """Get all categories (both system and user's custom categories)"""
    categories = db.query(Category).filter(
        ((Category.user_id == current_user.id) | (Category.user_id.is_(None))) &
        (Category.is_deleted == False)
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
        (Category.type == TransactionType.INCOME) &
        (Category.is_deleted == False)
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
        (Category.type == TransactionType.EXPENSE) &
        (Category.is_deleted == False)
    ).all()

    return categories

@router.post("/", response_model=CategoryResponse, status_code=status.HTTP_201_CREATED)
async def create_category(
        category_data: CategoryCreate,
        background_tasks: BackgroundTasks,
        current_user = Depends(get_current_user),
        db: Session = Depends(get_db)
):
    """Create a new custom category"""
    # Check if category with same name and type already exists for this user
    existing_category = db.query(Category).filter(
        Category.user_id == current_user.id,
        Category.name == category_data.name,
        Category.type == category_data.type,
        Category.is_deleted == False
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
        user_id=current_user.id,  # Set user_id to mark as custom category
        # Removed: sync_version
        updated_at=datetime.utcnow()
    )

    # Update user timestamp only (removed sync version)
    current_user.updated_at = datetime.utcnow()

    db.add(category)
    db.commit()
    db.refresh(category)

    # Notify about new category
    background_tasks.add_task(
        notify_user,
        current_user.id,
        "category_created",
        {
            "category": {
                "id": category.id,
                "name": category.name,
                "type": category.type.value
            }
        }
    )

    return category

@router.put("/{category_id}", response_model=CategoryResponse)
async def update_category(
        category_id: int,
        category_data: CategoryCreate,
        background_tasks: BackgroundTasks,
        current_user = Depends(get_current_user),
        db: Session = Depends(get_db)
):
    """Update a custom category"""
    category = db.query(Category).filter(
        Category.id == category_id,
        Category.user_id == current_user.id,
        Category.is_deleted == False
    ).first()

    if not category:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Category not found or you don't have permission to update it"
        )

    # Check for duplicate name (excluding current category)
    existing_category = db.query(Category).filter(
        Category.user_id == current_user.id,
        Category.name == category_data.name,
        Category.type == category_data.type,
        Category.id != category_id,
        Category.is_deleted == False
    ).first()

    if existing_category:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Category with name '{category_data.name}' and type '{category_data.type.value}' already exists"
        )

    category.name = category_data.name
    category.type = category_data.type
    category.color = category_data.color
    category.icon = category_data.icon

    # Update timestamp only (removed sync version)
    category.updated_at = datetime.utcnow()

    # Update user timestamp only (removed sync version)
    current_user.updated_at = datetime.utcnow()

    db.commit()
    db.refresh(category)

    # Notify about category update
    background_tasks.add_task(
        notify_user,
        current_user.id,
        "category_updated",
        {
            "category_id": category.id
        }
    )

    return category

@router.delete("/{category_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_category(
        category_id: int,
        background_tasks: BackgroundTasks,
        current_user = Depends(get_current_user),
        db: Session = Depends(get_db)
):
    """Soft delete a custom category (only user's own categories)"""
    category = db.query(Category).filter(
        Category.id == category_id,
        Category.user_id == current_user.id,
        Category.is_deleted == False
    ).first()

    if not category:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Category not found or you don't have permission to delete it"
        )

    # Check if category is being used by any transactions
    from app.models.models import Transaction
    transaction_count = db.query(Transaction).filter(
        Transaction.category_id == category_id,
        Transaction.is_deleted == False
    ).count()

    if transaction_count > 0:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Cannot delete category. It is being used by {transaction_count} transactions."
        )

    # Soft delete the category
    category.is_deleted = True
    # Removed: sync_version increment
    category.updated_at = datetime.utcnow()

    # Update user timestamp only (removed sync version)
    current_user.updated_at = datetime.utcnow()

    db.commit()

    # Notify about category deletion
    background_tasks.add_task(
        notify_user,
        current_user.id,
        "category_deleted",
        {
            "category_id": category_id
        }
    )

    return None