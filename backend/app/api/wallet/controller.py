from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app.auth import get_current_user
from app.api.wallet.model import WalletCreate, WalletUpdate, WalletResponse
from app.api.wallet.service import WalletService

router = APIRouter()

@router.get("/user/total")
def get_user_total_balance(
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return WalletService.get_user_total_balance(db, current_user)


@router.post("/", response_model=WalletResponse, status_code=status.HTTP_201_CREATED)
def create_wallet(
    wallet_data: WalletCreate,
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return WalletService.create_wallet(db, current_user, wallet_data)


@router.get("/", response_model=List[WalletResponse])
def get_wallets(
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return WalletService.get_wallets(db, current_user)


@router.get("/{wallet_id}", response_model=WalletResponse)
def get_wallet(
    wallet_id: int,
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return WalletService.get_wallet(db, current_user, wallet_id)


@router.get("/{wallet_id}/balance")
def get_wallet_balance(
    wallet_id: int,
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return WalletService.get_wallet_balance(db, current_user, wallet_id)


@router.put("/{wallet_id}", response_model=WalletResponse)
def update_wallet(
    wallet_id: int,
    wallet_data: WalletUpdate,
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return WalletService.update_wallet(db, current_user, wallet_id, wallet_data)


@router.delete("/{wallet_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_wallet(
    wallet_id: int,
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db)
):
    WalletService.delete_wallet(db, current_user, wallet_id)
    return