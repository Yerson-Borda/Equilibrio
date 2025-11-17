from sqlalchemy import Column, ForeignKey, Integer, String, Enum
from sqlalchemy.orm import relationship
from app.database import Base
from app.dto.enums.transaction_type import TransactionType

class Category(Base):
    __tablename__ = "categories"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    type = Column(Enum(TransactionType), nullable=False)
    color = Column(String, default="#000000")
    icon = Column(String)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    
    user = relationship("User", back_populates="categories")
    transactions = relationship("Transaction", back_populates="category")