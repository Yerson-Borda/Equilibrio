from pydantic import BaseModel
from typing import Optional
from datetime import datetime, date
from decimal import Decimal
from app.utils.enums.transaction_type import TransactionType

class TransactionBase(BaseModel):
    name: str 
    amount: Decimal
    note: Optional[str] = None
    type: TransactionType
    transaction_date: date
    wallet_id: int
    category_id: int

class TransactionCreate(TransactionBase):
    tags: list[int] = []
    pass

class TransactionResponse(TransactionBase):
    id: int
    user_id: int
    created_at: datetime
    tags: list[str]
    receipt_url: Optional[str] = None

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