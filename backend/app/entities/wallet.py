from sqlalchemy import Column, ForeignKey, Integer, String, DateTime, DECIMAL, Enum, Boolean
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.utils.enums.wallet_type import WalletType
from app.database import Base

class Wallet(Base):
    __tablename__ = "wallets"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    currency = Column(String, nullable=False, default="USD")
    balance = Column(DECIMAL(10, 2), default=0.00)
    wallet_type = Column(Enum(WalletType), nullable=False, default=WalletType.CASH)
    card_number = Column(String, nullable=True)
    color = Column(String, default="#3B82F6")
    user_id = Column(Integer, ForeignKey("users.id"))
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    # is_goal = Column(Boolean, default=False)

    owner = relationship("User", back_populates="wallets")
    transactions = relationship("Transaction", back_populates="wallet")
    goal = relationship("Goal", back_populates="wallet", uselist=False)