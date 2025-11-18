from sqlalchemy.orm import Session
from fastapi import HTTPException, status
from app.entities.category import Category
from app.utils.enums.transaction_type import TransactionType
from app.api.category.model import CategoryCreate

class CategoryService:

    @staticmethod
    def get_all_categories(db: Session, user_id: int):
        """Return system categories + user's categories"""
        return (
            db.query(Category)
            .filter((Category.user_id == user_id) | (Category.user_id.is_(None)))
            .all()
        )

    @staticmethod
    def get_categories_by_type(db: Session, user_id: int, category_type: TransactionType):
        """Return categories filtered by type (income/expense)"""
        return (
            db.query(Category)
            .filter(
                ((Category.user_id == user_id) | (Category.user_id.is_(None)))
                & (Category.type == category_type)
            )
            .all()
        )

    @staticmethod
    def create_category(db: Session, user_id: int, data: CategoryCreate):
        """Create a custom category"""
        existing = (
            db.query(Category)
            .filter(
                Category.user_id == user_id,
                Category.name == data.name,
                Category.type == data.type,
            )
            .first()
        )

        if existing:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Category '{data.name}' of type '{data.type.value}' already exists",
            )

        category = Category(
            name=data.name,
            type=data.type,
            color=data.color,
            icon=data.icon,
            user_id=user_id,
        )

        db.add(category)
        db.commit()
        db.refresh(category)
        return category

    @staticmethod
    def delete_category(db: Session, category_id: int, user_id: int):
        """Delete a custom category (owned by user only)"""
        category = (
            db.query(Category)
            .filter(Category.id == category_id, Category.user_id == user_id)
            .first()
        )

        if not category:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Category not found or you don't have permission to delete it",
            )

        db.delete(category)
        db.commit()