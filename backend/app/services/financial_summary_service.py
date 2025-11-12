from sqlalchemy.orm import Session
from datetime import date
from datetime import date
from sqlalchemy.orm import Session
from sqlalchemy import extract
from app.models.models import FinancialSummary, Transaction, TransactionType, Wallet, WalletType

def update_monthly_summary(db: Session, user_id: int, transaction):
    """Updates or creates the monthly summary based on the transaction."""
    today = date.today()

    # Get or create current month’s financial summary
    summary = (
        db.query(FinancialSummary)
        .filter_by(user_id=user_id, month=today.month, year=today.year)
        .first()
    )

    if not summary:
        summary = FinancialSummary(
            user_id=user_id,
            month=today.month,
            year=today.year,
        )
        db.add(summary)
        db.commit()
        db.refresh(summary)

    # Update summary based on transaction type
    if transaction.type == TransactionType.INCOME:
        summary.total_income += transaction.amount

        # If income goes into a saving account, count it as "saved"
        wallet = db.query(Wallet).get(transaction.wallet_id)
        if wallet.wallet_type == WalletType.SAVING_ACCOUNT:
            summary.total_saved += transaction.amount

    elif transaction.type == TransactionType.EXPENSE:
        summary.total_spent += transaction.amount

    db.commit()
    db.refresh(summary)
    return summary


def recalculate_monthly_summary(db: Session, user_id: int):
    """Recalculates the monthly summary after wallet or transaction deletions."""
    today = date.today()
    summary = (
        db.query(FinancialSummary)
        .filter_by(user_id=user_id, month=today.month, year=today.year)
        .first()
    )

    # If no summary exists, nothing to recalc
    if not summary:
        return

    # Get all remaining transactions for this user in the current month
    transactions = (
        db.query(Transaction)
        .join(Wallet, Transaction.wallet_id == Wallet.id)
        .filter(
            Wallet.user_id == user_id,
            extract('month', Transaction.created_at) == today.month,
            extract('year', Transaction.created_at) == today.year
        )
        .all()
    )

    # Reset totals
    total_income = 0
    total_spent = 0
    total_saved = 0

    for txn in transactions:
        if txn.type == TransactionType.INCOME:
            total_income += txn.amount
            wallet = db.query(Wallet).get(txn.wallet_id)
            if wallet and wallet.wallet_type == WalletType.SAVING_ACCOUNT:
                total_saved += txn.amount
        elif txn.type == TransactionType.EXPENSE:
            total_spent += txn.amount

    summary.total_income = total_income
    summary.total_spent = total_spent
    summary.total_saved = total_saved

    db.commit()
    db.refresh(summary)
    return summary