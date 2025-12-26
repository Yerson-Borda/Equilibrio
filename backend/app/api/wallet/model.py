from pydantic import BaseModel
from typing import Optional
from datetime import datetime
from decimal import Decimal
from app.utils.enums.wallet_type import WalletType

class WalletBase(BaseModel):
    name: str
    currency: str = "USD"
    wallet_type: WalletType
    card_number: Optional[str] = None
    color: str = "#3B82F6"

class WalletCreate(WalletBase):
    balance: Decimal = Decimal('0.00')
    pass

class WalletUpdate(BaseModel):
    name: Optional[str] = None
    currency: Optional[str] = None
    wallet_type: Optional[WalletType] = None
    balance: Optional[Decimal] = None
    card_number: Optional[str] = None
    color: Optional[str] = None

class WalletResponse(WalletBase):
    id: int
    balance: Decimal
    user_id: int
    created_at: datetime

    class Config:
        from_attributes = True