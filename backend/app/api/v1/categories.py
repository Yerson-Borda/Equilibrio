from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app.models.models import Category
from app.schemas.schemas import CategoryResponse
from app.auth import get_current_user

router = APIRouter()

@router.get("/", response_model=List[CategoryResponse])
def get_categories(
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    # Get both system categories (user_id is None) and user's custom categories
    categories = db.query(Category).filter(
        (Category.user_id == current_user.id) | (Category.user_id.is_(None))
    ).all()
    
    return categories