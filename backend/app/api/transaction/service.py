from sqlalchemy.orm import Session
from fastapi import HTTPException
from datetime import date
from decimal import Decimal
from app.api.transaction.model import TransactionResponse
from app.entities.tag import Tag
from app.entities.transaction import Transaction
from app.entities.wallet import Wallet
from app.entities.category import Category
from app.utils.enums.transaction_type import TransactionType
from app.api.budget import service as budget_service
from app.api.financial_summary.service import update_monthly_summary
from app.services.currency_service import currency_service
from fastapi import UploadFile
from app.core.file_settings import (
    TRANSACTION_UPLOAD_DIR,
    ALLOWED_EXTENSIONS,
    MAX_FILE_SIZE
)
from uuid import uuid4
from pathlib import Path
import shutil

def save_receipt(user_id: int, file: UploadFile) -> str:
    ext = Path(file.filename).suffix.lower()

    if ext not in ALLOWED_EXTENSIONS:
        raise HTTPException(400, "Invalid file type")

    file.file.seek(0, 2)
    size = file.file.tell()
    file.file.seek(0)

    if size > MAX_FILE_SIZE:
        raise HTTPException(400, "File too large")

    filename = f"{user_id}_{uuid4()}{ext}"
    path = TRANSACTION_UPLOAD_DIR / filename

    with open(path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    return f"/static/receipts/{filename}"

def create_transaction(db: Session, user_id: int, data, receipt: UploadFile | None = None):
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

    receipt_url = None
    if receipt:
        receipt_url = save_receipt(user_id, receipt)

    transaction = Transaction(
        name=data.name,
        amount=data.amount,
        note=data.note,
        type=data.type,
        transaction_date=data.transaction_date,
        wallet_id=data.wallet_id,
        category_id=data.category_id,
        user_id=user_id,
        receipt_url=receipt_url
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

    # Convert tag IDs → Tag ORM instances
    if data.tags:
        tag_objects = db.query(Tag).filter(
            Tag.id.in_(data.tags),
            Tag.user_id == user_id
        ).all()
        transaction.tags = tag_objects

    db.add(transaction)
    db.commit()
    db.refresh(transaction)

    update_monthly_summary(db, user_id, transaction)

    return _serialize_transaction(transaction)

def get_wallet_transactions(db: Session, user_id: int, wallet_id: int, limit: int):
    wallet = db.query(Wallet).filter(
        Wallet.id == wallet_id,
        Wallet.user_id == user_id
    ).first()

    if not wallet:
        raise HTTPException(404, "Wallet not found or forbidden")

    txs = db.query(Transaction).filter(
        Transaction.wallet_id == wallet_id
    ).order_by(
        Transaction.transaction_date.desc(),
        Transaction.id.desc()
    ).limit(limit).all()

    return _serialize_transactions(txs)


def get_user_transactions(db: Session, user_id: int):
    txs = db.query(Transaction).filter(
        Transaction.user_id == user_id
    ).order_by(Transaction.transaction_date.desc()).all()

    return _serialize_transactions(txs)


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
        name="Transfers",
        amount=data.amount,
        note=data.note,
        type=TransactionType.TRANSFER,
        transaction_date=today,
        wallet_id=source_wallet.id,
        category_id=transfer_out.id,
        user_id=user_id
    )

    dest_tx = Transaction(
        name="Transfers",
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

def get_transactions_by_tag(db: Session, user_id: int, tag_id: int):
    txs = (
        db.query(Transaction)
        .join(Transaction.tags)
        .filter(
            Tag.id == tag_id,
            Transaction.user_id == user_id
        )
        .order_by(Transaction.transaction_date.desc())
        .all()
    )

    return _serialize_transactions(txs)

def search_transactions_by_tag(db: Session, user_id: int, text: str):
    txs = (
        db.query(Transaction)
        .join(Transaction.tags)
        .filter(
            Tag.name.ilike(f"%{text}%"),
            Transaction.user_id == user_id
        )
        .all()
    )

    return _serialize_transactions(txs)

def _serialize_transaction(tx: Transaction) -> TransactionResponse:
    return TransactionResponse(
        id=tx.id,
        name=tx.name,
        amount=tx.amount,
        note=tx.note,
        type=tx.type,
        transaction_date=tx.transaction_date,
        wallet_id=tx.wallet_id,
        category_id=tx.category_id,
        user_id=tx.user_id,
        created_at=tx.created_at,
        tags=[t.name for t in tx.tags],
        receipt_url=tx.receipt_url
    )

def _serialize_transactions(txs: list[Transaction]) -> list[TransactionResponse]:
    return [_serialize_transaction(tx) for tx in txs]