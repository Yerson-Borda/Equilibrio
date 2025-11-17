from fastapi import FastAPI
from app.api.auth.controller import router as users
from app.api.budget.controller import router as budget
from app.api.category.controller import router as categories
from app.api.financial_summary.controller import router as financial_summary
from app.api.transaction.controller import router as transactions
from app.api.wallet.controller import router as wallets

def register_routers(app: FastAPI):
    app.include_router(users, prefix="/api/users", tags=["users"])
    app.include_router(wallets, prefix="/api/wallets", tags=["wallets"])
    app.include_router(transactions, prefix="/api/transactions", tags=["transactions"])
    app.include_router(categories, prefix="/api/categories", tags=["categories"])
    app.include_router(budget, prefix="/api/budget", tags=["budget"])
    app.include_router(financial_summary, prefix="/api/financial_summary", tags=["financial_summary"])