from sqlalchemy import Boolean, Column, ForeignKey, Integer, String, DateTime, Text, DECIMAL, Date, Enum
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
import enum
from app.database import Base

class TransactionType(enum.Enum):
    INCOME = "income"
    EXPENSE = "expense"
    TRANSFER = "transfer"

class WalletType(enum.Enum):
    DEBIT_CARD = "debit_card"
    CASH = "cash"
    CREDIT_CARD = "credit_card"
    SAVING_ACCOUNT = "saving_account"
    INVESTMENT = "investment"
    LOAN = "loan"
    MORTGAGE = "mortgage"

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    full_name = Column(String)
    phone_number = Column(String, nullable=True)
    date_of_birth = Column(Date, nullable=True)
    avatar_url = Column(String, nullable=True)
    default_currency = Column(String, default="USD")
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())

    wallets = relationship("Wallet", back_populates="owner")
    categories = relationship("Category", back_populates="user")
    transactions = relationship("Transaction", back_populates="user")
    budgets = relationship("Budget", back_populates="user", cascade="all, delete-orphan")

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

    owner = relationship("User", back_populates="wallets")
    transactions = relationship("Transaction", back_populates="wallet")

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

class Transaction(Base):
    __tablename__ = "transactions"

    id = Column(Integer, primary_key=True, index=True)
    amount = Column(DECIMAL(10, 2), nullable=False)
    note = Column(Text, nullable=True)
    type = Column(Enum(TransactionType), nullable=False)
    transaction_date = Column(Date, nullable=False, server_default=func.now())
    wallet_id = Column(Integer, ForeignKey("wallets.id"))
    category_id = Column(Integer, ForeignKey("categories.id"))
    user_id = Column(Integer, ForeignKey("users.id"))
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    wallet = relationship("Wallet", back_populates="transactions")
    category = relationship("Category", back_populates="transactions")
    user = relationship("User", back_populates="transactions")

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

    user = relationship("User", back_populates="budgets")
