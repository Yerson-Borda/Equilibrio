from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles
from app.database import engine, get_db
from app.models import models
from app.api.v1 import budget, users, wallets, transactions, categories, financial_summary
from sqlalchemy.orm import Session

models.Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="Personal Finance Tracker API",
    description="Backend API for personal finance management application",
    version="1.0.0"
)

app.mount("/static", StaticFiles(directory="static"), name="static")

def create_default_categories():
    db = next(get_db())
    try:
        # checking if categories already exist
        existing = db.query(models.Category).first()
        if not existing:
            default_categories = [
                # Expense categories
                {"name": "Foods & Drinks", "type": models.TransactionType.EXPENSE, "color": "#18D2E6", "icon": "utensils"},
                {"name": "Shopping", "type": models.TransactionType.EXPENSE, "color": "#F6BDE9", "icon": "shopping-bag"},
                {"name": "Housing", "type": models.TransactionType.EXPENSE, "color": "#FFAB4C", "icon": "home"},
                {"name": "Transportation", "type": models.TransactionType.EXPENSE, "color": "#AE45FF", "icon": "bus"},
                {"name": "Vehicle", "type": models.TransactionType.EXPENSE, "color": "#403BD7", "icon": "car"},
                {"name": "Entertainment", "type": models.TransactionType.EXPENSE, "color": "#E53838", "icon": "film"},
                {"name": "Communication", "type": models.TransactionType.EXPENSE, "color": "#FFCB66", "icon": "phone"},
                {"name": "Investments", "type": models.TransactionType.EXPENSE, "color": "#53D258", "icon": "chart-line"},
                {"name": "Others", "type": models.TransactionType.EXPENSE, "color": "#4E5C75", "icon": "ellipsis-h"},
                
                # Income category
                {"name": "Refunds", "type": models.TransactionType.INCOME, "color": "#18E637", "icon": "arrow-in"},
                {"name": "Rental Income", "type": models.TransactionType.INCOME, "color": "#D90BAA", "icon": "home"},
                {"name": "Gambling", "type": models.TransactionType.INCOME, "color": "#AC5C02", "icon": "cards"},
                {"name": "Lending", "type": models.TransactionType.INCOME, "color": "#1676BA", "icon": "arrow-out"},
                {"name": "Sale", "type": models.TransactionType.INCOME, "color": "#ABC418", "icon": "coins"},
                {"name": "Wage, invoices", "type": models.TransactionType.INCOME, "color": "#16A0A4", "icon": "money-hand"},
                {"name": "Gifts", "type": models.TransactionType.INCOME, "color": "#CFA147", "icon": "gift"},
                {"name": "Dues & grants", "type": models.TransactionType.INCOME, "color": "#0F6AD2", "icon": "check"},
                {"name": "Interests", "type": models.TransactionType.INCOME, "color": "#4520DE", "icon": "pencentage"},
                {"name": "Others", "type": models.TransactionType.INCOME, "color": "#4E5C75", "icon": "ellipsis-h"},
                
                # Transfer category
                {"name": "Transfer Out", "type": models.TransactionType.TRANSFER, "color": "#FFA500", "icon": "arrow-up"},
                {"name": "Transfer In", "type": models.TransactionType.TRANSFER, "color": "#32CD32", "icon": "arrow-down"},
            ]
            
            for cat_data in default_categories:
                category = models.Category(**cat_data)
                db.add(category)
            
            db.commit()
    except Exception as e:
        db.rollback()
    finally:
        db.close()

create_default_categories()

app.include_router(users.router, prefix="/api/v1/users", tags=["users"])
app.include_router(wallets.router, prefix="/api/v1/wallets", tags=["wallets"])
app.include_router(transactions.router, prefix="/api/v1/transactions", tags=["transactions"])
app.include_router(categories.router, prefix="/api/v1/categories", tags=["categories"])
app.include_router(budget.router, prefix="/api/v1/budget", tags=["budget"])
app.include_router(financial_summary.router, prefix="/api/v1/financial_summary", tags=["financial_summary"])

@app.get("/")
def root():
    return {
        "message": "Equilibrio API",
        "version": "1.0.0",
        "docs": "/docs",
        "redoc": "/redoc"
    }

@app.get("/health")
def health_check():
    return {"status": "healthy"}