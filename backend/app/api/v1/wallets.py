from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app.models.models import Wallet, Transaction
from app.schemas.schemas import WalletCreate, WalletResponse, WalletUpdate
from app.auth import get_current_user
from decimal import Decimal
from app.services.currency_service import currency_service

router = APIRouter()

@router.get("/user/total")
def get_user_total_balance(
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    wallets = db.query(Wallet).filter(Wallet.user_id == current_user.id).all()
    total_balance = Decimal('0.0')
    breakdown = []
    
    for wallet in wallets:
        if wallet.currency.upper() == current_user.default_currency.upper():
            total_balance += wallet.balance
            converted_balance = wallet.balance
            exchange_rate = 1.0
        else:
            converted_balance = currency_service.convert_amount(
                wallet.balance, 
                wallet.currency, 
                current_user.default_currency
            )
            exchange_rate = currency_service.get_exchange_rate(wallet.currency, current_user.default_currency)
            total_balance += converted_balance
        
        breakdown.append({
            "wallet_id": wallet.id,
            "wallet_name": wallet.name,
            "wallet_type": wallet.wallet_type.value,
            "original_balance": float(wallet.balance),
            "original_currency": wallet.currency,
            "converted_balance": float(converted_balance),
            "converted_currency": current_user.default_currency,
            "exchange_rate_used": float(exchange_rate)
        })
    
    return {
        "total_balance": float(total_balance), 
        "currency": current_user.default_currency,
        "breakdown": breakdown
    }

@router.post("/", response_model=WalletResponse, status_code=status.HTTP_201_CREATED)
def create_wallet(
    wallet_data: WalletCreate,
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    existing_wallet = db.query(Wallet).filter(
        Wallet.user_id == current_user.id,
        Wallet.name == wallet_data.name
    ).first()
    
    if existing_wallet:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Wallet with name '{wallet_data.name}' already exists"
        )
    
    wallet = Wallet(
        name=wallet_data.name,
        currency=wallet_data.currency,
        wallet_type=wallet_data.wallet_type,
        card_number=wallet_data.card_number,
        color=wallet_data.color,
        user_id=current_user.id,
        balance=wallet_data.initial_balance
    )
    
    db.add(wallet)
    db.commit()
    db.refresh(wallet)
    
    return wallet

@router.get("/", response_model=List[WalletResponse])
def get_wallets(
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    wallets = db.query(Wallet).filter(Wallet.user_id == current_user.id).all()
    return wallets

@router.get("/{wallet_id}", response_model=WalletResponse)
def get_wallet(
    wallet_id: int,
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    wallet = db.query(Wallet).filter(
        Wallet.id == wallet_id,
        Wallet.user_id == current_user.id
    ).first()
    
    if not wallet:
        raise HTTPException(status_code=404, detail="Wallet not found")
    
    return wallet

@router.get("/{wallet_id}/balance")
def get_wallet_balance(
    wallet_id: int,
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    wallet = db.query(Wallet).filter(
        Wallet.id == wallet_id,
        Wallet.user_id == current_user.id
    ).first()
    
    if not wallet:
        raise HTTPException(status_code=404, detail="Wallet not found")
    
    return {"balance": float(wallet.balance)}

@router.put("/{wallet_id}", response_model=WalletResponse)
def update_wallet(
    wallet_id: int,
    wallet_data: WalletUpdate,
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Update wallet details including initial balance"""
    wallet = db.query(Wallet).filter(
        Wallet.id == wallet_id,
        Wallet.user_id == current_user.id
    ).first()
    
    if not wallet:
        raise HTTPException(status_code=404, detail="Wallet not found")
    
    # Check for duplicate name (excluding current wallet)
    if wallet_data.name is not None:
        existing_wallet = db.query(Wallet).filter(
            Wallet.user_id == current_user.id,
            Wallet.name == wallet_data.name,
            Wallet.id != wallet_id
        ).first()
        
        if existing_wallet:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Wallet with name '{wallet_data.name}' already exists"
            )
        wallet.name = wallet_data.name
    
    if wallet_data.initial_balance is not None:
        transactions = db.query(Transaction).filter(Transaction.wallet_id == wallet_id).all()
        
        net_transaction_amount = Decimal('0.0')
        for transaction in transactions:
            if transaction.type.value == "income":
                net_transaction_amount += transaction.amount
            else:  # expense
                net_transaction_amount -= transaction.amount
        
        # Calculate what the new balance would be with the new initial balance
        new_balance = wallet_data.initial_balance + net_transaction_amount
        
        # Check if new balance would be negative
        if new_balance < Decimal('0.0'):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Cannot set initial balance to {wallet_data.initial_balance}. "
                       f"This would result in a negative wallet balance of {new_balance} after accounting for {len(transactions)} transactions."
            )
        
        wallet.balance = new_balance
    
    if wallet_data.currency is not None:
        wallet.currency = wallet_data.currency
    
    if wallet_data.wallet_type is not None:
        wallet.wallet_type = wallet_data.wallet_type
    
    if wallet_data.card_number is not None:
        wallet.card_number = wallet_data.card_number
    
    if wallet_data.color is not None:
        wallet.color = wallet_data.color
    
    db.commit()
    db.refresh(wallet)
    
    return wallet

@router.delete("/{wallet_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_wallet(
    wallet_id: int,
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Delete a wallet and all its transactions"""
    wallet = db.query(Wallet).filter(
        Wallet.id == wallet_id,
        Wallet.user_id == current_user.id
    ).first()
    
    if not wallet:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Wallet not found"
        )
    
    # Delete all transactions associated with this wallet
    db.query(Transaction).filter(Transaction.wallet_id == wallet_id).delete()
    
    # Delete the wallet
    db.delete(wallet)
    db.commit()
    
    return