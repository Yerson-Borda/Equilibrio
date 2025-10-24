from pydantic import BaseModel, EmailStr
from typing import Optional, List
from datetime import datetime, date
from decimal import Decimal

class UserBase(BaseModel):
    email: EmailStr
    full_name: Optional[str] = None
    default_currency: str = "USD"

class UserCreate(UserBase):
    password: str

class UserResponse(UserBase):
    id: int
    is_active: bool
    created_at: datetime

    class Config:
        from_attributes = True

class WalletBase(BaseModel):
    name: str
    currency: str = "USD"

class WalletCreate(WalletBase):
    pass

class WalletResponse(WalletBase):
    id: int
    balance: Decimal
    user_id: int
    created_at: datetime

    class Config:
        from_attributes = True

class CategoryBase(BaseModel):
    name: str
    type: str
    color: Optional[str] = "#000000"

class CategoryCreate(CategoryBase):
    pass

class CategoryResponse(CategoryBase):
    id: int
    user_id: int

    class Config:
        from_attributes = True

class TransactionBase(BaseModel):
    amount: Decimal
    description: Optional[str] = None
    type: str
    transaction_date: date
    wallet_id: int
    category_id: int

class TransactionCreate(TransactionBase):
    pass

class TransactionResponse(TransactionBase):
    id: int
    user_id: int
    created_at: datetime

    class Config:
        from_attributes = True

class Token(BaseModel):
    access_token: str
    token_type: str

class TokenData(BaseModel):
    email: Optional[str] = None