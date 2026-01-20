from sqlalchemy import Column, ForeignKey, Integer, DateTime, String, Text, DECIMAL, Date, Enum
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.database import Base
from app.utils.enums.transaction_type import TransactionType

class Transaction(Base):
    __tablename__ = "transactions"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(30), nullable=False)
    amount = Column(DECIMAL(10, 2), nullable=False)
    note = Column(Text, nullable=True)
    type = Column(Enum(TransactionType), nullable=False)
    receipt_url = Column(String, nullable=True)
    transaction_date = Column(Date, nullable=False, server_default=func.now())
    wallet_id = Column(Integer, ForeignKey("wallets.id"))
    category_id = Column(Integer, ForeignKey("categories.id"))
    user_id = Column(Integer, ForeignKey("users.id"))
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    wallet = relationship("Wallet", back_populates="transactions")
    category = relationship("Category", back_populates="transactions")
    user = relationship("User", back_populates="transactions")
    tags = relationship("Tag", secondary="transaction_tags", back_populates="transactions")