from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app.models.models import Wallet
from app.schemas.schemas import WalletCreate, WalletResponse
from app.auth import get_current_user

router = APIRouter()

@router.post("/", response_model=WalletResponse)
def create_wallet(
    wallet_data: WalletCreate,
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    wallet = Wallet(
        name=wallet_data.name,
        currency=wallet_data.currency,
        user_id=current_user.id
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