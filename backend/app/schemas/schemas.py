from pydantic import BaseModel, EmailStr, Field
from typing import Optional, List
from datetime import datetime, date
from decimal import Decimal
from app.models.models import TransactionType, WalletType

class UserBase(BaseModel):
    email: EmailStr
    full_name: Optional[str] = None
    phone_number: Optional[str] = None
    date_of_birth: Optional[date] = None
    avatar_url: Optional[str] = None
    default_currency: str = "USD"

class UserCreate(UserBase):
    password: str = Field(..., min_length=6, max_length=128)

class UserResponse(UserBase):
    id: int
    is_active: bool
    created_at: datetime

    class Config:
        from_attributes = True

class UserUpdate(BaseModel):
    email: Optional[EmailStr] = None
    full_name: Optional[str] = None
    phone_number: Optional[str] = None
    date_of_birth: Optional[date] = None
    avatar_url: Optional[str] = None
    default_currency: Optional[str] = None
    password: Optional[str] = Field(None, min_length=6, max_length=128)

class WalletBase(BaseModel):
    name: str
    currency: str = "USD"
    wallet_type: WalletType
    initial_balance: Decimal = Decimal('0.00')
    card_number: Optional[str] = None
    color: str = "#3B82F6"

class WalletCreate(WalletBase):
    pass

class WalletUpdate(BaseModel):
    name: Optional[str] = None
    currency: Optional[str] = None
    wallet_type: Optional[WalletType] = None
    initial_balance: Optional[Decimal] = None
    card_number: Optional[str] = None
    color: Optional[str] = None

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
    icon: Optional[str] = None

class CategoryCreate(CategoryBase):
    pass

class CategoryResponse(CategoryBase):
    id: int
    user_id: Optional[int]

    class Config:
        from_attributes = True

class TransactionBase(BaseModel):
    amount: Decimal
    description: Optional[str] = None
    note: Optional[str] = None
    type: TransactionType
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

class TransferCreate(BaseModel):
    source_wallet_id: int
    destination_wallet_id: int
    amount: Decimal
    note: Optional[str] = None

class TransferResponse(BaseModel):
    message: str
    source_transaction: TransactionResponse
    destination_transaction: TransactionResponse
    exchange_rate: float
    converted_amount: float

class Token(BaseModel):
    access_token: str
    token_type: str

class TokenData(BaseModel):
    email: Optional[str] = None