from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.database import get_db
from app.entities.financial_summary import FinancialSummary
from app.entities.user import User
from app.api.financial_summary.model import FinancialSummaryResponse
from app.auth import get_current_user
from datetime import date

router = APIRouter()

@router.get("/current", response_model=FinancialSummaryResponse)
def get_current_summary(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    today = date.today()
    summary = (
        db.query(FinancialSummary)
        .filter_by(user_id=current_user.id, month=today.month, year=today.year)
        .first()
    )

    if not summary:
        summary = FinancialSummary(
            user_id=current_user.id,
            month=today.month,
            year=today.year
        )
        db.add(summary)
        db.commit()
        db.refresh(summary)

    return summary
