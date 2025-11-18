from app.database import get_db
from app.entities.category import Category
from app.utils.enums.transaction_type import TransactionType

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

def seed_categories():
    db = next(get_db())
    try:
        if db.query(Category).count() == 0:
            for data in default_categories:
                db.add(Category(**data))
            db.commit()
    finally:
        db.close()