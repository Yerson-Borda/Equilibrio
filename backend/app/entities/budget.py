from sqlalchemy import Column, ForeignKey, Integer, DateTime, DECIMAL, Date
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.database import Base
from datetime import date

class Budget(Base):
    __tablename__ = "budgets"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)

    monthly_limit = Column(DECIMAL(10, 2), default=0.00)
    daily_limit = Column(DECIMAL(10, 2), default=0.00)

    monthly_spent = Column(DECIMAL(10, 2), default=0.00)
    daily_spent = Column(DECIMAL(10, 2), default=0.00)

    month = Column(Integer, nullable=False)
    year = Column(Integer, nullable=False)

    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())

    last_updated_date = Column(Date, default=date.today)

    user = relationship("User", back_populates="budgets")