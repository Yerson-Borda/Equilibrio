from sqlalchemy.orm import Session
from datetime import date
from app.models.models import FinancialSummary, TransactionType, Wallet, WalletType

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
