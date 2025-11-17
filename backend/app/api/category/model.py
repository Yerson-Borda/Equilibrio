from pydantic import BaseModel
from typing import Optional
from app.dto.enums.transaction_type import TransactionType

class CategoryBase(BaseModel):
    name: str
    type: TransactionType
    color: Optional[str] = "#000000"
    icon: Optional[str] = None

class CategoryCreate(CategoryBase):
    pass

class CategoryResponse(CategoryBase):
    id: int
    user_id: Optional[int]

    class Config:
        from_attributes = True