from fastapi import FastAPI
from app.database import engine, get_db
from app.models import models
from app.api.v1 import users, wallets, transactions, categories
from sqlalchemy.orm import Session

models.Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="Personal Finance Tracker API",
    description="Backend API for personal finance management application",
    version="1.0.0"
)

# creating default categories here for the system
def create_default_categories():
    db = next(get_db())
    try:
        # checking if categories already exist
        existing = db.query(models.Category).first()
        if not existing:
            default_categories = [
                # Expense categories
                {"name": "Foods & Drinks", "type": models.TransactionType.EXPENSE, "color": "#FF6B6B", "icon": "utensils"},
                {"name": "Shopping", "type": models.TransactionType.EXPENSE, "color": "#4ECDC4", "icon": "shopping-bag"},
                {"name": "Housing", "type": models.TransactionType.EXPENSE, "color": "#45B7D1", "icon": "home"},
                {"name": "Transportation", "type": models.TransactionType.EXPENSE, "color": "#96CEB4", "icon": "bus"},
                {"name": "Vehicle", "type": models.TransactionType.EXPENSE, "color": "#FFEAA7", "icon": "car"},
                {"name": "Entertainment", "type": models.TransactionType.EXPENSE, "color": "#DDA0DD", "icon": "dice"},
                {"name": "Communication", "type": models.TransactionType.EXPENSE, "color": "#98D8C8", "icon": "phone"},
                {"name": "Investments", "type": models.TransactionType.EXPENSE, "color": "#F7DC6F", "icon": "coin"},
                {"name": "Others", "type": models.TransactionType.EXPENSE, "color": "#85C1E9", "icon": "ellipsis-h"},
                
                # Income category (only one)
                {"name": "Income", "type": models.TransactionType.INCOME, "color": "#98D8C8", "icon": "money-bill"},
            ]
            
            for cat_data in default_categories:
                category = models.Category(**cat_data)
                db.add(category)
            
            db.commit()
            print("✅ Default categories created successfully")
    except Exception as e:
        print(f"❌ Error creating default categories: {e}")
        db.rollback()
    finally:
        db.close()

create_default_categories()

app.include_router(users.router, prefix="/api/v1/users", tags=["users"])
app.include_router(wallets.router, prefix="/api/v1/wallets", tags=["wallets"])
app.include_router(transactions.router, prefix="/api/v1/transactions", tags=["transactions"])
app.include_router(categories.router, prefix="/api/v1/categories", tags=["categories"])

@app.get("/")
def root():
    return {
        "message": "Personal Finance Tracker API",
        "version": "1.0.0",
        "docs": "/docs"
    }

@app.get("/health")
def health_check():
    return {"status": "healthy"}