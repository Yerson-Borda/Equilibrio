from fastapi import HTTPException
from sqlalchemy import func
from sqlalchemy.orm import Session
from datetime import date
from decimal import Decimal

from app.api.category.service import CategoryService
from app.entities.category_limit import CategoryLimit
from app.entities.transaction import Transaction
from app.utils.enums.transaction_type import TransactionType

class CategoryLimitService:

    @staticmethod
    def set_limit(db: Session, user_id: int, category_id: int, limit_value: Decimal):
        limit = (
            db.query(CategoryLimit)
            .filter_by(user_id=user_id, category_id=category_id)
            .first()
        )

        if not limit:
            limit = CategoryLimit(
                user_id=user_id,
                category_id=category_id,
                monthly_limit=limit_value
            )
            db.add(limit)
        else:
            limit.monthly_limit = limit_value

        db.commit()
        db.refresh(limit)
        return limit

    @staticmethod
    def delete_limit(db: Session, user_id: int, category_id: int):
        limit = (
            db.query(CategoryLimit)
            .filter_by(user_id=user_id, category_id=category_id)
            .first()
        )

        if not limit:
            raise HTTPException(404, "Category limit not found")

        db.delete(limit)
        db.commit()

    @staticmethod
    def get_all_category_limits_with_spent(db: Session, user_id: int):
        today = date.today()

        categories = CategoryService.get_all_categories(db, user_id)

        categories = [cat for cat in categories if cat.type == TransactionType.EXPENSE]

        results = []
        for cat in categories:

            # total spent in this category for current month
            total_spent = (
                db.query(func.sum(Transaction.amount))
                .filter(
                    Transaction.category_id == cat.id,
                    Transaction.user_id == user_id,
                    func.extract('month', Transaction.transaction_date) == today.month,
                    func.extract('year', Transaction.transaction_date) == today.year,
                    Transaction.type == TransactionType.EXPENSE
                )
                .scalar()
            )

            total_spent = total_spent or Decimal("0.00")

            # fetch limit if exists
            limit = (
                db.query(CategoryLimit)
                .filter_by(user_id=user_id, category_id=cat.id)
                .first()
            )

            results.append({
                "category_id": cat.id,
                "category_name": cat.name,
                "category_color": cat.color,
                "category_icon": cat.icon,
                "monthly_limit": limit.monthly_limit if limit else Decimal("0.00"),
                "monthly_spent": total_spent
            })

        return results

