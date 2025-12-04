from pydantic import BaseModel
from decimal import Decimal

class CategoryLimitBase(BaseModel):
    monthly_limit: Decimal

class CategoryLimitCreate(CategoryLimitBase):
    pass

class CategoryLimitUpdate(BaseModel):
    monthly_limit: Decimal

class CategoryLimitResponse(CategoryLimitBase):
    id: int
    category_id: int
    user_id: int

    class Config:
        from_attributes = True

class CategoryLimitOverviewItem(BaseModel):
    category_id: int
    category_name: str
    category_color: str | None
    category_icon: str | None
    monthly_limit: Decimal
    monthly_spent: Decimal