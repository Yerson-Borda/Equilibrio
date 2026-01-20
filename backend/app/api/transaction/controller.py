from fastapi import Form
from typing import Optional
from datetime import date
from decimal import Decimal
from app.utils.enums.transaction_type import TransactionType

from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.core.auth import get_current_user
from app.entities.user import User
from app.api.transaction.model import (
    TransactionCreate, TransactionResponse,
    TransferCreate, TransferResponse, TransferPreviewResponse
)
from app.api.transaction import service
from fastapi import UploadFile, File

router = APIRouter()

class TransactionCreateForm:
    def __init__(
        self,
        name: str = Form(...),
        amount: Decimal = Form(...),
        type: TransactionType = Form(...),
        transaction_date: date = Form(...),
        wallet_id: int = Form(...),
        category_id: int = Form(...),
        note: Optional[str] = Form(None),
        tags: Optional[str] = Form(None),
    ):
        self.data = TransactionCreate(
            name=name,
            amount=amount,
            note=note,
            type=type,
            transaction_date=transaction_date,
            wallet_id=wallet_id,
            category_id=category_id,
            tags=[int(t) for t in tags.split(",")] if tags else []
        )

@router.post("/", response_model=TransactionResponse, status_code=status.HTTP_201_CREATED)
def create_transaction(
    form: TransactionCreateForm = Depends(),
    receipt: UploadFile | None = File(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return service.create_transaction(
        db=db,
        user_id=current_user.id,
        data=form.data,
        receipt=receipt
    )

@router.get("/wallet/{wallet_id}", response_model=list[TransactionResponse])
def get_wallet_transactions(
    wallet_id: int,
    limit: int = Query(10, ge=1, le=100),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return service.get_wallet_transactions(db, current_user.id, wallet_id, limit)


@router.get("/filter/by-tag/{tag_id}", response_model=list[TransactionResponse])
def get_transactions_by_tag(
    tag_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return service.get_transactions_by_tag(db, current_user.id, tag_id)


@router.get("/", response_model=list[TransactionResponse])
def get_user_transactions(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return service.get_user_transactions(db, current_user.id)


@router.delete("/{transaction_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_transaction(
    transaction_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    service.delete_transaction(db, current_user.id, transaction_id)
    return


@router.post("/transfer", response_model=TransferResponse)
def transfer_funds(
    transfer_data: TransferCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return service.transfer_funds(db, current_user.id, transfer_data)

@router.get("/transfer/preview", response_model=TransferPreviewResponse)
def preview_transfer(
    source_wallet_id: int,
    destination_wallet_id: int,
    amount: Decimal,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return service.preview_transfer(
        db=db,
        user_id=current_user.id,
        source_wallet_id=source_wallet_id,
        destination_wallet_id=destination_wallet_id,
        amount=amount
    )
