from sqlalchemy.orm import Session
from decimal import Decimal
from fastapi import HTTPException
from app.entities.goal import Goal
from app.entities.wallet import Wallet
from app.entities.transaction import Transaction
from app.utils.enums.wallet_type import WalletType
from app.utils.enums.transaction_type import TransactionType
from fastapi import UploadFile
from pathlib import Path
from uuid import uuid4
import shutil
from app.core.file_settings import (
    GOAL_UPLOAD_DIR,
    ALLOWED_EXTENSIONS,
    MAX_FILE_SIZE
)


def create_goal(db: Session, user, data, image: UploadFile | None = None):
    image_url = None
    if image:
        image_url = save_goal_image(user.id, image)

    wallet = Wallet(
        name=data.title,
        currency=data.currency,
        wallet_type=WalletType.GOAL,
        balance=Decimal("0.00"),
        user_id=user.id
    )
    db.add(wallet)
    db.flush()

    goal = Goal(
        title=data.title,
        description=data.description,
        image=image_url,
        deadline=data.deadline,
        goal_amount=data.goal_amount,
        wallet_id=wallet.id,
        user_id=user.id
    )

    db.add(goal)
    db.commit()
    db.refresh(goal)
    return goal


def get_goals(db: Session, user):
    return db.query(Goal).filter(Goal.user_id == user.id).all()


def get_goal(db: Session, user, goal_id: int):
    goal = db.query(Goal).filter_by(id=goal_id, user_id=user.id).first()
    if not goal:
        raise HTTPException(404, "Goal not found")
    return goal


def update_goal(db: Session, user, goal_id: int, data):
    goal = get_goal(db, user, goal_id)

    for field, value in data.dict(exclude_unset=True).items():
        setattr(goal, field, value)

    # keep wallet name in sync
    if data.title:
        goal.wallet.name = data.title

    db.commit()
    db.refresh(goal)
    return goal


def delete_goal(db: Session, user, goal_id: int):
    goal = (
        db.query(Goal)
        .join(Wallet)
        .filter(
            Goal.id == goal_id,
            Wallet.user_id == user.id
        )
        .first()
    )

    if not goal:
        raise HTTPException(404, "Goal not found")

    goal_wallet = goal.wallet

    # ✅ CASE 1: No money saved → delete freely
    if goal.amount_saved == 0:
        db.delete(goal_wallet)
        db.commit()
        return

    # ✅ CASE 2: Money exists → must have a saving account
    saving_wallet = (
        db.query(Wallet)
        .filter(
            Wallet.user_id == user.id,
            Wallet.wallet_type == WalletType.SAVING_ACCOUNT,
            Wallet.id != goal_wallet.id
        )
        .first()
    )

    if not saving_wallet:
        raise HTTPException(
            400,
            "Cannot delete goal with saved money: no saving account available"
        )

    # Transfer funds
    saving_wallet.balance += goal_wallet.balance

    db.delete(goal_wallet)
    db.commit()

def update_goal_progress(db: Session, transaction: Transaction):
    if transaction.type != TransactionType.TRANSFER:
        return

    wallet = db.query(Wallet).get(transaction.wallet_id)

    if wallet.wallet_type != WalletType.GOAL:
        return

    goal = db.query(Goal).filter(Goal.wallet_id == wallet.id).first()
    if not goal:
        return

    goal.amount_saved += transaction.amount
    db.commit()

def save_goal_image(user_id: int, file: UploadFile) -> str:
    ext = Path(file.filename).suffix.lower()

    if ext not in ALLOWED_EXTENSIONS:
        raise HTTPException(400, "Invalid image type")

    file.file.seek(0, 2)
    size = file.file.tell()
    file.file.seek(0)

    if size > MAX_FILE_SIZE:
        raise HTTPException(400, "Image too large")

    filename = f"{user_id}_{uuid4()}{ext}"
    path = GOAL_UPLOAD_DIR / filename

    with open(path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    return f"/static/goals/{filename}"
