from sqlalchemy import Column, Integer, ForeignKey, DateTime, DECIMAL
from sqlalchemy.sql import func
from app.database import Base

class MonthlySavingsGoal(Base):
    __tablename__ = "monthly_savings_goals"

    id = Column(Integer, primary_key=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)

    month = Column(Integer, nullable=False)
    year = Column(Integer, nullable=False)

    target_amount = Column(DECIMAL(10, 2), default=0.00)
    current_saved = Column(DECIMAL(10, 2), default=0.00)

    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
