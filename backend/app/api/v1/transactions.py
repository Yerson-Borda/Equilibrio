from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from app.database import get_db
from app.models.models import Transaction, Wallet, Category, User, TransactionType
from app.schemas.schemas import TransactionCreate, TransactionResponse, TransferCreate, TransferResponse
from app.auth import get_current_user
from datetime import datetime, date
from decimal import Decimal
from app.services.currency_service import currency_service

router = APIRouter()

@router.post("/", response_model=TransactionResponse, status_code=status.HTTP_201_CREATED)
def create_transaction(
    transaction_data: TransactionCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    wallet = db.query(Wallet).filter(
        Wallet.id == transaction_data.wallet_id,
        Wallet.user_id == current_user.id
    ).first()
    
    if not wallet:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Wallet not found or you don't have permission"
        )
    
    category = db.query(Category).filter(Category.id == transaction_data.category_id).first()
    if not category:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Category not found"
        )
    
    transaction = Transaction(
        amount=transaction_data.amount,
        note=transaction_data.note,
        type=transaction_data.type,
        transaction_date=transaction_data.transaction_date,
        wallet_id=transaction_data.wallet_id,
        category_id=transaction_data.category_id,
        user_id=current_user.id
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
    
    db.add(transaction)
    db.commit()
    db.refresh(transaction)
    
    return transaction

@router.get("/wallet/{wallet_id}", response_model=List[TransactionResponse])
def get_wallet_transactions(
    wallet_id: int,
    limit: int = Query(10, ge=1, le=100, description="Number of transactions to fetch"),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    wallet = db.query(Wallet).filter(
        Wallet.id == wallet_id,
        Wallet.user_id == current_user.id
    ).first()
    
    if not wallet:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Wallet not found or you don't have permission"
        )
    
    transactions = db.query(Transaction).filter(
        Transaction.wallet_id == wallet_id
    ).order_by(Transaction.transaction_date.desc(), Transaction.id.desc()).limit(limit).all()
    
    return transactions

@router.get("/", response_model=List[TransactionResponse])
def get_user_transactions(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    transactions = db.query(Transaction).filter(
        Transaction.user_id == current_user.id
    ).order_by(Transaction.transaction_date.desc()).all()
    
    return transactions

@router.delete("/{transaction_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_transaction(
    transaction_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Delete a transaction and recalculate wallet balance"""
    transaction = db.query(Transaction).filter(
        Transaction.id == transaction_id,
        Transaction.user_id == current_user.id
    ).first()
    
    if not transaction:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Transaction not found"
        )
    
    wallet = db.query(Wallet).filter(
        Wallet.id == transaction.wallet_id,
        Wallet.user_id == current_user.id
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
    
    # Delete the transaction
    db.delete(transaction)
    db.commit()
    
    return

@router.post("/transfer", response_model=TransferResponse, status_code=status.HTTP_200_OK)
def transfer_funds(
    transfer_data: TransferCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Transfer funds between wallets with currency conversion"""
    source_wallet = db.query(Wallet).filter(
        Wallet.id == transfer_data.source_wallet_id,
        Wallet.user_id == current_user.id
    ).first()
    
    if not source_wallet:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Source wallet not found"
        )
    
    destination_wallet = db.query(Wallet).filter(
        Wallet.id == transfer_data.destination_wallet_id,
        Wallet.user_id == current_user.id
    ).first()
    
    if not destination_wallet:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Destination wallet not found"
        )
    
    if source_wallet.id == destination_wallet.id:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Cannot transfer to the same wallet"
        )
    
    if source_wallet.balance < transfer_data.amount:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Insufficient balance in source wallet. Available: {source_wallet.balance} {source_wallet.currency}"
        )
    
    transfer_out_category = db.query(Category).filter(
        Category.name == "Transfer Out",
        Category.user_id.is_(None)
    ).first()
    
    transfer_in_category = db.query(Category).filter(
        Category.name == "Transfer In", 
        Category.user_id.is_(None)
    ).first()
    
    if not transfer_out_category or not transfer_in_category:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Transfer categories not found"
        )
    
    # Calculate converted amount if currencies are different
    if source_wallet.currency == destination_wallet.currency:
        converted_amount = transfer_data.amount
        exchange_rate = 1.0
    else:
        # Convert from source currency to destination currency
        converted_amount = currency_service.convert_amount(
            transfer_data.amount,
            source_wallet.currency,
            destination_wallet.currency
        )
        exchange_rate = currency_service.get_exchange_rate(
            source_wallet.currency,
            destination_wallet.currency
        )
    
    # Create transfer transactions
    today = date.today()
    
    # Source transaction (Transfer Out)
    source_transaction = Transaction(
        amount=transfer_data.amount,
        note=transfer_data.note,
        type=TransactionType.TRANSFER,
        transaction_date=today,
        wallet_id=source_wallet.id,
        category_id=transfer_out_category.id,
        user_id=current_user.id
    )
    
    # Destination transaction (Transfer In)
    destination_transaction = Transaction(
        amount=converted_amount,
        note=transfer_data.note,
        type=TransactionType.TRANSFER,
        transaction_date=today,
        wallet_id=destination_wallet.id,
        category_id=transfer_in_category.id,
        user_id=current_user.id
    )
    
    source_wallet.balance -= transfer_data.amount
    destination_wallet.balance += converted_amount
    
    db.add(source_transaction)
    db.add(destination_transaction)
    db.commit()
    db.refresh(source_transaction)
    db.refresh(destination_transaction)
    
    return {
        "message": "Transfer completed successfully",
        "source_transaction": source_transaction,
        "destination_transaction": destination_transaction,
        "exchange_rate": float(exchange_rate),
        "converted_amount": float(converted_amount)
    }