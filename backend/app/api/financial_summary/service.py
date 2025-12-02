from decimal import Decimal
from sqlalchemy.orm import Session
from datetime import date
from datetime import date
from sqlalchemy.orm import Session
from sqlalchemy import extract
from app.api.financial_summary.model import FinancialSummaryResponse
from app.entities.financial_summary import FinancialSummary
from app.entities.transaction import Transaction
from app.entities.user import User
from app.entities.wallet import Wallet
from app.utils.enums.transaction_type import TransactionType
from app.utils.enums.wallet_type import WalletType
from app.services.currency_service import currency_service

def update_monthly_summary(db: Session, user_id: int, transaction):
    today = date.today()

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

    # Get user + wallet
    wallet = db.query(Wallet).get(transaction.wallet_id)
    user = wallet.owner

    display_currency = user.default_currency.upper()

    # Convert transaction amount into user's default currency
    converted_amount = currency_service.convert_amount(
        transaction.amount,
        from_currency=wallet.currency,
        to_currency=display_currency,
    )

    if transaction.type == TransactionType.INCOME:
        summary.total_income += converted_amount

        if wallet.wallet_type == WalletType.SAVING_ACCOUNT:
            summary.total_saved += converted_amount

    elif transaction.type == TransactionType.EXPENSE:
        summary.total_spent += converted_amount

    db.commit()
    db.refresh(summary)
    return summary


def recalculate_monthly_summary(db: Session, user_id: int):
    today = date.today()

    summary = (
        db.query(FinancialSummary)
        .filter_by(user_id=user_id, month=today.month, year=today.year)
        .first()
    )

    if not summary:
        return

    transactions = (
        db.query(Transaction)
        .join(Wallet, Transaction.wallet_id == Wallet.id)
        .filter(
            Wallet.user_id == user_id,
            extract('month', Transaction.created_at) == today.month,
            extract('year', Transaction.created_at) == today.year,
        )
        .all()
    )

    total_income = Decimal("0")
    total_spent = Decimal("0")
    total_saved = Decimal("0")

    # get user currency
    user = db.query(User).get(user_id)
    display_currency = user.default_currency.upper()

    for txn in transactions:
        wallet = db.query(Wallet).get(txn.wallet_id)

        converted = currency_service.convert_amount(
            txn.amount,
            from_currency=wallet.currency,
            to_currency=display_currency
        )

        if txn.type == TransactionType.INCOME:
            total_income += converted
            if wallet.wallet_type == WalletType.SAVING_ACCOUNT:
                total_saved += converted

        elif txn.type == TransactionType.EXPENSE:
            total_spent += converted

    summary.total_income = total_income
    summary.total_spent = total_spent
    summary.total_saved = total_saved

    db.commit()
    db.refresh(summary)
    return summary

def get_user_current_summary(db: Session, user: User):
    today = date.today()

    # Load summary
    summary = (
        db.query(FinancialSummary)
        .filter_by(user_id=user.id, month=today.month, year=today.year)
        .first()
    )

    # Create if missing
    if not summary:
        summary = FinancialSummary(
            user_id=user.id,
            month=today.month,
            year=today.year
        )
        db.add(summary)
        db.commit()
        db.refresh(summary)

    display_currency = user.default_currency.upper()

    # Determine "base currency": wallet currency or USD fallback
    wallets = db.query(Wallet).filter(Wallet.user_id == user.id).all()
    base_currency = wallets[0].currency if wallets else "USD"

    # Convert totals
    converted_income = currency_service.convert_amount(
        summary.total_income,
        from_currency=base_currency,
        to_currency=display_currency,
    )

    converted_spent = currency_service.convert_amount(
        summary.total_spent,
        from_currency=base_currency,
        to_currency=display_currency,
    )

    converted_saved = currency_service.convert_amount(
        summary.total_saved,
        from_currency=base_currency,
        to_currency=display_currency,
    )

    return FinancialSummaryResponse(
        id=summary.id,
        month=summary.month,
        year=summary.year,
        total_income=converted_income,
        total_spent=converted_spent,
        total_saved=converted_saved,
        created_at=summary.created_at,
        currency=display_currency
    )