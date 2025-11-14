from fastapi import APIRouter, Depends, HTTPException, status, Query, BackgroundTasks
from sqlalchemy.orm import Session
from typing import List, Optional
from datetime import datetime, date
from decimal import Decimal
from app.database import get_db
from app.models.models import Transaction, Wallet, Category, User, TransactionType
from app.schemas.schemas import TransactionCreate, TransactionResponse, TransferCreate, TransferResponse
from app.auth import get_current_user
from app.services.currency_service import currency_service
from app.core.websocket_manager import manager
import json

router = APIRouter()

async def notify_user(user_id: int, event_type: str, data: dict):
    """Notify user about data changes via WebSocket"""
    message = {
        "event": event_type,
        "data": data,
        "timestamp": datetime.utcnow().isoformat()
    }
    await manager.send_personal_message(json.dumps(message), user_id)

@router.post("/", response_model=TransactionResponse, status_code=status.HTTP_201_CREATED)
async def create_transaction(
        transaction_data: TransactionCreate,
        background_tasks: BackgroundTasks,
        current_user: User = Depends(get_current_user),
        db: Session = Depends(get_db)
):
    wallet = db.query(Wallet).filter(
        Wallet.id == transaction_data.wallet_id,
        Wallet.user_id == current_user.id,
        Wallet.is_deleted == False
    ).first()

    if not wallet:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Wallet not found or you don't have permission"
        )

    category = db.query(Category).filter(
        Category.id == transaction_data.category_id,
        Category.is_deleted == False
    ).first()

    if not category:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Category not found"
        )

    transaction = Transaction(
        amount=transaction_data.amount,
        description=transaction_data.description,
        note=transaction_data.note,
        type=transaction_data.type,
        transaction_date=transaction_data.transaction_date,
        wallet_id=transaction_data.wallet_id,
        category_id=transaction_data.category_id,
        user_id=current_user.id,
        updated_at=datetime.utcnow()
    )

    if transaction_data.type == TransactionType.INCOME:
        wallet.balance += transaction_data.amount
    else:  # expense
        if wallet.balance < transaction_data.amount:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Insufficient balance in wallet"
            )
        wallet.balance -= transaction_data.amount

    wallet.updated_at = datetime.utcnow()
    current_user.updated_at = datetime.utcnow()

    db.add(transaction)
    db.commit()
    db.refresh(transaction)

    # Notify user about new transaction
    background_tasks.add_task(
        notify_user,
        current_user.id,
        "transaction_created",
        {
            "transaction": {
                "id": transaction.id,
                "amount": float(transaction.amount),
                "description": transaction.description,
                "type": transaction.type.value,
                "wallet_id": transaction.wallet_id,
                "category_id": transaction.category_id
            },
            "wallet_balance": float(wallet.balance)
        }
    )

    return transaction

# Update other transaction endpoints similarly...
# Remove sync_version references and add WebSocket notifications

@router.put("/{transaction_id}", response_model=TransactionResponse)
async def update_transaction(
        transaction_id: int,
        transaction_data: TransactionCreate,
        background_tasks: BackgroundTasks,
        current_user: User = Depends(get_current_user),
        db: Session = Depends(get_db)
):
    """Update a transaction"""
    transaction = db.query(Transaction).filter(
        Transaction.id == transaction_id,
        Transaction.user_id == current_user.id,
        Transaction.is_deleted == False
    ).first()

    if not transaction:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Transaction not found"
        )

    # Store old values for balance recalculation
    old_amount = transaction.amount
    old_type = transaction.type
    old_wallet_id = transaction.wallet_id

    # Get the wallet
    wallet = db.query(Wallet).filter(
        Wallet.id == transaction_data.wallet_id,
        Wallet.user_id == current_user.id,
        Wallet.is_deleted == False
    ).first()

    if not wallet:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Wallet not found"
        )

    category = db.query(Category).filter(
        Category.id == transaction_data.category_id,
        Category.is_deleted == False
    ).first()

    if not category:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Category not found"
        )

    # Reverse old transaction effect on balance
    if old_wallet_id == transaction_data.wallet_id:
        # Same wallet - simple adjustment
        if old_type == TransactionType.INCOME:
            wallet.balance -= old_amount
        else:  # expense
            wallet.balance += old_amount
    else:
        # Different wallet - need to update both wallets
        old_wallet = db.query(Wallet).filter(Wallet.id == old_wallet_id).first()
        if old_wallet:
            if old_type == TransactionType.INCOME:
                old_wallet.balance -= old_amount
            else:  # expense
                old_wallet.balance += old_amount
            old_wallet.updated_at = datetime.utcnow()

    # Apply new transaction effect
    if transaction_data.type == TransactionType.INCOME:
        wallet.balance += transaction_data.amount
    else:  # expense
        if wallet.balance < transaction_data.amount:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Insufficient balance in wallet"
            )
        wallet.balance -= transaction_data.amount

    # Update transaction fields
    transaction.amount = transaction_data.amount
    transaction.description = transaction_data.description
    transaction.note = transaction_data.note
    transaction.type = transaction_data.type
    transaction.transaction_date = transaction_data.transaction_date
    transaction.wallet_id = transaction_data.wallet_id
    transaction.category_id = transaction_data.category_id
    transaction.updated_at = datetime.utcnow()

    wallet.updated_at = datetime.utcnow()
    current_user.updated_at = datetime.utcnow()

    db.commit()
    db.refresh(transaction)

    # Notify about update
    background_tasks.add_task(
        notify_user,
        current_user.id,
        "transaction_updated",
        {
            "transaction_id": transaction.id,
            "wallet_balance": float(wallet.balance)
        }
    )

    return transaction

@router.delete("/{transaction_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_transaction(
        transaction_id: int,
        background_tasks: BackgroundTasks,
        current_user: User = Depends(get_current_user),
        db: Session = Depends(get_db)
):
    """Soft delete a transaction and recalculate wallet balance"""
    transaction = db.query(Transaction).filter(
        Transaction.id == transaction_id,
        Transaction.user_id == current_user.id,
        Transaction.is_deleted == False
    ).first()

    if not transaction:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Transaction not found"
        )

    wallet = db.query(Wallet).filter(
        Wallet.id == transaction.wallet_id,
        Wallet.user_id == current_user.id,
        Wallet.is_deleted == False
    ).first()

    if not wallet:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Wallet not found"
        )

    # Recalculate wallet balance by reversing the transaction
    if transaction.type == TransactionType.INCOME:
        wallet.balance -= transaction.amount
        # Check if balance would go negative (shouldn't happen with proper validation)
        if wallet.balance < 0:
            wallet.balance = 0  # Prevent negative balance
    else:
        wallet.balance += transaction.amount

    # Soft delete the transaction
    transaction.is_deleted = True
    transaction.updated_at = datetime.utcnow()

    # Update wallet
    wallet.updated_at = datetime.utcnow()
    current_user.updated_at = datetime.utcnow()

    db.commit()

    # Notify about deletion
    background_tasks.add_task(
        notify_user,
        current_user.id,
        "transaction_deleted",
        {
            "transaction_id": transaction_id,
            "wallet_balance": float(wallet.balance)
        }
    )

    return