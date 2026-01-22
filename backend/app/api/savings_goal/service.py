from sqlalchemy.orm import Session
from datetime import date
from decimal import Decimal
from app.entities.monthly_savings_goal import MonthlySavingsGoal
from app.entities.wallet import Wallet
from app.utils.enums.wallet_type import WalletType
from app.utils.enums.transaction_type import TransactionType
from app.entities.transaction import Transaction

def get_or_create_current_savings_goal(db: Session, user_id: int):
    today = date.today()

    goal = db.query(MonthlySavingsGoal).filter_by(
        user_id=user_id,
        month=today.month,
        year=today.year
    ).first()

    if not goal:
        goal = MonthlySavingsGoal(
            user_id=user_id,
            month=today.month,
            year=today.year,
            target_amount=Decimal("0.00"),
            current_saved=Decimal("0.00")
        )
        db.add(goal)
        db.commit()
        db.refresh(goal)

    return goal

def update_savings_target(db: Session, user_id: int, target_amount: Decimal):
    goal = get_or_create_current_savings_goal(db, user_id)
    goal.target_amount = target_amount
    db.commit()
    db.refresh(goal)
    return goal

def record_savings(db: Session, transaction: Transaction):
    wallet = db.query(Wallet).get(transaction.wallet_id)

    if not wallet:
        return

    if wallet.wallet_type not in (WalletType.SAVING_ACCOUNT, WalletType.GOAL):
        return

    if transaction.type not in (
        TransactionType.INCOME,
        TransactionType.TRANSFER,
    ):
        return

    goal = get_or_create_current_savings_goal(db, transaction.user_id)
    goal.current_saved += transaction.amount

    db.commit()

