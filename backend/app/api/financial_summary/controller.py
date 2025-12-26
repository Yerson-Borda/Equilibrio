from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.database import get_db
from app.entities.user import User
from app.api.financial_summary.model import FinancialSummaryResponse
from app.core.auth import get_current_user
from app.api.financial_summary.service import get_user_current_summary

router = APIRouter()

@router.get("/current", response_model=FinancialSummaryResponse)
def get_current_summary(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    return get_user_current_summary(db, current_user)

