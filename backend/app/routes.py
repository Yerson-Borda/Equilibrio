from fastapi import FastAPI
from app.api.auth.controller import router as auth
from app.api.budget.controller import router as budget
from app.api.category.controller import router as categories
from app.api.financial_summary.controller import router as financial_summary
from app.api.transaction.controller import router as transactions
from app.api.wallet.controller import router as wallets
from app.api.user.controller import router as users
from app.api.analytics.controller import router as analytics
from app.api.tag.controller import router as tags
from app.api.category_limits.controller import router as category_limits
from app.api.goal.controller import router as goal
from app.api.savings_goal.controller import router as savings_goal

def register_routers(app: FastAPI):
    app.include_router(auth, prefix="/api/auth", tags=["auth"])
    app.include_router(users, prefix="/api/users", tags=["users"])
    app.include_router(wallets, prefix="/api/wallets", tags=["wallets"])
    app.include_router(transactions, prefix="/api/transactions", tags=["transactions"])
    app.include_router(categories, prefix="/api/categories", tags=["categories"])
    app.include_router(budget, prefix="/api/budget", tags=["budget"])
    app.include_router(financial_summary, prefix="/api/financial_summary", tags=["financial_summary"])
    app.include_router(analytics, prefix="/api/analytics", tags=["analytics"])
    app.include_router(tags, prefix="/api/tags", tags=["tags"])
    app.include_router(category_limits, prefix="/api/limits", tags=["category_limits"])
    app.include_router(goal, prefix="/api/goals", tags=["goals"])
    app.include_router(savings_goal, prefix="/api/savings_goal", tags=["savings_goal"])
