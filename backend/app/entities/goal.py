from sqlalchemy import (
    Column, Integer, String, Date, DECIMAL, ForeignKey
)
from sqlalchemy.orm import relationship
from app.database import Base

class Goal(Base):
    __tablename__ = "goals"

    id = Column(Integer, primary_key=True)
    title = Column(String, nullable=False)
    description = Column(String, nullable=True)
    image = Column(String, nullable=True)

    deadline = Column(Date, nullable=True)

    goal_amount = Column(DECIMAL(10, 2), nullable=False)
    amount_saved = Column(DECIMAL(10, 2), default=0.00)

    wallet_id = Column(Integer, ForeignKey("wallets.id"), unique=True)
    user_id = Column(Integer, ForeignKey("users.id"))

    wallet = relationship("Wallet", back_populates="goal")
    user = relationship("User", back_populates="goals")