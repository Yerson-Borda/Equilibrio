from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from app.database import get_db
from app.models.models import Transaction, Wallet, Category, User, TransactionType
from app.schemas.schemas import TransactionCreate, TransactionResponse
from app.auth import get_current_user
from datetime import datetime

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
        description=transaction_data.description,
        type=transaction_data.type,
        transaction_date=transaction_data.transaction_date,
        wallet_id=transaction_data.wallet_id,
        category_id=transaction_data.category_id,
        user_id=current_user.id
    )
    
    # Fixed: Use TransactionType enum for comparison
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

# Get all transactions for current user
@router.get("/", response_model=List[TransactionResponse])
def get_user_transactions(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    transactions = db.query(Transaction).filter(
        Transaction.user_id == current_user.id
    ).order_by(Transaction.transaction_date.desc()).all()
    
    return transactions