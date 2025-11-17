from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles
from app.database import engine, get_db
from app.entities.category import Category
from app.dto.enums.transaction_type import TransactionType
from app.database import Base
from .routes import register_routers

app = FastAPI(
    title="Personal Finance Tracker API",
    description="Backend API for personal finance management application",
    version="1.0.0"
)

Base.metadata.create_all(bind=engine)

app.mount("/static", StaticFiles(directory="static"), name="static")

def create_default_categories():
    db = next(get_db())
    try:
        # checking if categories already exist
        existing = db.query(Category).first()
        if not existing:
            default_categories = [
                # Expense categories
                {"name": "Foods & Drinks", "type": TransactionType.EXPENSE, "color": "#18D2E6", "icon": "utensils"},
                {"name": "Shopping", "type": TransactionType.EXPENSE, "color": "#F6BDE9", "icon": "shopping-bag"},
                {"name": "Housing", "type": TransactionType.EXPENSE, "color": "#FFAB4C", "icon": "home"},
                {"name": "Transportation", "type": TransactionType.EXPENSE, "color": "#AE45FF", "icon": "bus"},
                {"name": "Vehicle", "type": TransactionType.EXPENSE, "color": "#403BD7", "icon": "car"},
                {"name": "Entertainment", "type": TransactionType.EXPENSE, "color": "#E53838", "icon": "film"},
                {"name": "Communication", "type": TransactionType.EXPENSE, "color": "#FFCB66", "icon": "phone"},
                {"name": "Investments", "type": TransactionType.EXPENSE, "color": "#53D258", "icon": "chart-line"},
                {"name": "Others", "type": TransactionType.EXPENSE, "color": "#4E5C75", "icon": "ellipsis-h"},
                
                # Income category
                {"name": "Refunds", "type": TransactionType.INCOME, "color": "#18E637", "icon": "arrow-in"},
                {"name": "Rental Income", "type": TransactionType.INCOME, "color": "#D90BAA", "icon": "home"},
                {"name": "Gambling", "type": TransactionType.INCOME, "color": "#AC5C02", "icon": "cards"},
                {"name": "Lending", "type": TransactionType.INCOME, "color": "#1676BA", "icon": "arrow-out"},
                {"name": "Sale", "type": TransactionType.INCOME, "color": "#ABC418", "icon": "coins"},
                {"name": "Wage, invoices", "type": TransactionType.INCOME, "color": "#16A0A4", "icon": "money-hand"},
                {"name": "Gifts", "type": TransactionType.INCOME, "color": "#CFA147", "icon": "gift"},
                {"name": "Dues & grants", "type": TransactionType.INCOME, "color": "#0F6AD2", "icon": "check"},
                {"name": "Interests", "type": TransactionType.INCOME, "color": "#4520DE", "icon": "pencentage"},
                {"name": "Others", "type": TransactionType.INCOME, "color": "#4E5C75", "icon": "ellipsis-h"},
                
                # Transfer category
                {"name": "Transfer Out", "type": TransactionType.TRANSFER, "color": "#FFA500", "icon": "arrow-up"},
                {"name": "Transfer In", "type": TransactionType.TRANSFER, "color": "#32CD32", "icon": "arrow-down"},
            ]
            
            for cat_data in default_categories:
                category = Category(**cat_data)
                db.add(category)
            
            db.commit()
    except Exception as e:
        db.rollback()
    finally:
        db.close()

create_default_categories()

register_routers(app)

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