from sqlalchemy.orm import Session
from fastapi import HTTPException
from datetime import date
from decimal import Decimal
from app.entities.transaction import Transaction
from app.entities.wallet import Wallet
from app.entities.category import Category
from app.utils.enums.transaction_type import TransactionType
from app.api.budget import service as budget_service
from app.api.financial_summary.service import update_monthly_summary
from app.services.currency_service import currency_service

def create_transaction(db: Session, user_id: int, data):
    wallet = db.query(Wallet).filter(
        Wallet.id == data.wallet_id,
        Wallet.user_id == user_id
    ).first()

    if not wallet:
        raise HTTPException(
            status_code=404,
            detail="Wallet not found or you don't have permission"
        )

    category = db.query(Category).filter(Category.id == data.category_id).first()
    if not category:
        raise HTTPException(status_code=404, detail="Category not found")

    transaction = Transaction(
        name=data.name,
        amount=data.amount,
        note=data.note,
        type=data.type,
        transaction_date=data.transaction_date,
        wallet_id=data.wallet_id,
        category_id=data.category_id,
        user_id=user_id
    )

    # Update wallet balance
    if data.type == TransactionType.INCOME:
        wallet.balance += data.amount
    else:  # EXPENSE
        if wallet.balance < data.amount:
            raise HTTPException(400, "Insufficient balance")
        wallet.balance -= data.amount

        budget_service.record_expense(
            db=db,
            user_id=user_id,
            amount=float(data.amount)
        )

    db.add(transaction)
    db.commit()
    db.refresh(transaction)

    update_monthly_summary(db, user_id, transaction)

    return transaction


def get_wallet_transactions(db: Session, user_id: int, wallet_id: int, limit: int):
    wallet = db.query(Wallet).filter(
        Wallet.id == wallet_id,
        Wallet.user_id == user_id
    ).first()

    if not wallet:
        raise HTTPException(404, "Wallet not found or forbidden")

    return db.query(Transaction).filter(
        Transaction.wallet_id == wallet_id
    ).order_by(
        Transaction.transaction_date.desc(),
        Transaction.id.desc()
    ).limit(limit).all()


def get_user_transactions(db: Session, user_id: int):
    return db.query(Transaction).filter(
        Transaction.user_id == user_id
    ).order_by(Transaction.transaction_date.desc()).all()


def delete_transaction(db: Session, user_id: int, transaction_id: int):
    transaction = db.query(Transaction).filter(
        Transaction.id == transaction_id,
        Transaction.user_id == user_id
    ).first()

    if not transaction:
        raise HTTPException(404, "Transaction not found")

    wallet = db.query(Wallet).filter(
        Wallet.id == transaction.wallet_id,
        Wallet.user_id == user_id
    ).first()

    if not wallet:
        raise HTTPException(404, "Wallet not found")

    # Reverse transaction effect
    if transaction.type == TransactionType.INCOME:
        wallet.balance = max(Decimal('0'), wallet.balance - transaction.amount)
    else:
        wallet.balance += transaction.amount

        budget = budget_service.get_or_create_current_budget(db, user_id)
        budget.daily_spent = max(Decimal('0.00'), budget.daily_spent - transaction.amount)
        budget.monthly_spent = max(Decimal('0.00'), budget.monthly_spent - transaction.amount)

    db.delete(transaction)
    db.commit()


def transfer_funds(db: Session, user_id: int, data):
    source_wallet = db.query(Wallet).filter(
        Wallet.id == data.source_wallet_id,
        Wallet.user_id == user_id
    ).first()

    if not source_wallet:
        raise HTTPException(404, "Source wallet not found")

    destination_wallet = db.query(Wallet).filter(
        Wallet.id == data.destination_wallet_id,
        Wallet.user_id == user_id
    ).first()

    if not destination_wallet:
        raise HTTPException(404, "Destination wallet not found")

    if source_wallet.id == destination_wallet.id:
        raise HTTPException(400, "Cannot transfer to the same wallet")

    if source_wallet.balance < data.amount:
        raise HTTPException(400, "Insufficient balance in source wallet")

    transfer_out = db.query(Category).filter(
        Category.name == "Transfer Out", Category.user_id.is_(None)
    ).first()

    transfer_in = db.query(Category).filter(
        Category.name == "Transfer In", Category.user_id.is_(None)
    ).first()

    if not transfer_out or not transfer_in:
        raise HTTPException(500, "Transfer categories not found")

    # Conversion
    if source_wallet.currency == destination_wallet.currency:
        converted_amount = data.amount
        exchange_rate = 1.0
    else:
        converted_amount = currency_service.convert_amount(
            data.amount, source_wallet.currency, destination_wallet.currency
        )
        exchange_rate = currency_service.get_exchange_rate(
            source_wallet.currency, destination_wallet.currency
        )

    today = date.today()

    # Create transactions
    source_tx = Transaction(
        name="Transfer out",
        amount=data.amount,
        note=data.note,
        type=TransactionType.TRANSFER,
        transaction_date=today,
        wallet_id=source_wallet.id,
        category_id=transfer_out.id,
        user_id=user_id
    )

    dest_tx = Transaction(
        name="Transfer in",
        amount=converted_amount,
        note=data.note,
        type=TransactionType.TRANSFER,
        transaction_date=today,
        wallet_id=destination_wallet.id,
        category_id=transfer_in.id,
        user_id=user_id
    )

    source_wallet.balance -= data.amount
    destination_wallet.balance += converted_amount

    db.add(source_tx)
    db.add(dest_tx)
    db.commit()
    db.refresh(source_tx)
    db.refresh(dest_tx)

    return {
        "message": "Transfer completed successfully",
        "source_transaction": source_tx,
        "destination_transaction": dest_tx,
        "exchange_rate": float(exchange_rate),
        "converted_amount": float(converted_amount)
    }