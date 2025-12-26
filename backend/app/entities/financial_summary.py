from sqlalchemy import Column, ForeignKey, Integer, DateTime, DECIMAL
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.database import Base

class FinancialSummary(Base):
    __tablename__ = "financial_summaries"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)

    month = Column(Integer, nullable=False)
    year = Column(Integer, nullable=False)

    total_income = Column(DECIMAL(10, 2), default=0.00)
    total_spent = Column(DECIMAL(10, 2), default=0.00)
    total_saved = Column(DECIMAL(10, 2), default=0.00)

    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())

    user = relationship("User", back_populates="financial_summaries")