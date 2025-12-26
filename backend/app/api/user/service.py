import shutil
import uuid
from pathlib import Path
from sqlalchemy.orm import Session
from sqlalchemy.exc import IntegrityError
from fastapi import HTTPException, status, UploadFile
from app.entities.user import User
from app.entities.wallet import Wallet
from app.entities.transaction import Transaction
from app.utils.enums.transaction_type import TransactionType
from app.api.auth.model import UserUpdate
from app.core.security import get_password_hash
from app.core.file_settings import (
    AVATAR_UPLOAD_DIR,
    ALLOWED_EXTENSIONS,
    MAX_FILE_SIZE
)

class UserService:

    @staticmethod
    def get_detailed_user_info(db: Session, current_user: User):
        """Return user with stats"""
        wallet_count = (
            db.query(Wallet).filter(Wallet.user_id == current_user.id).count()
        )

        expense_count = (
            db.query(Transaction)
            .filter(
                Transaction.user_id == current_user.id,
                Transaction.type == TransactionType.EXPENSE,
            )
            .count()
        )

        income_count = (
            db.query(Transaction)
            .filter(
                Transaction.user_id == current_user.id,
                Transaction.type == TransactionType.INCOME,
            )
            .count()
        )

        return {
            "user": {
                "id": current_user.id,
                "email": current_user.email,
                "full_name": current_user.full_name,
                "phone_number": current_user.phone_number,
                "date_of_birth": current_user.date_of_birth,
                "avatar_url": current_user.avatar_url,
                "default_currency": current_user.default_currency,
                "created_at": current_user.created_at,
            },
            "stats": {
                "wallet_count": wallet_count,
                "total_transactions": expense_count + income_count,
                "expense_count": expense_count,
                "income_count": income_count,
            },
        }


    @staticmethod
    def update_user(db: Session, current_user: User, user_data: UserUpdate):
        """Update user's profile data"""
        try:
            if user_data.email is not None:
                existing_user = (
                    db.query(User)
                    .filter(
                        User.email == user_data.email,
                        User.id != current_user.id,
                    )
                    .first()
                )
                if existing_user:
                    raise HTTPException(
                        status_code=status.HTTP_409_CONFLICT,
                        detail="Email already registered by another user",
                    )
                current_user.email = user_data.email

            if user_data.full_name is not None:
                current_user.full_name = user_data.full_name

            if user_data.phone_number is not None:
                current_user.phone_number = user_data.phone_number

            if user_data.date_of_birth is not None:
                current_user.date_of_birth = user_data.date_of_birth

            if user_data.default_currency is not None:
                current_user.default_currency = user_data.default_currency.upper()

            if user_data.password is not None:
                current_user.hashed_password = get_password_hash(user_data.password)

            db.commit()
            db.refresh(current_user)
            return current_user

        except HTTPException:
            raise

        except IntegrityError as e:
            db.rollback()
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Email already registered by another user",
            )

        except Exception:
            db.rollback()
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Internal server error during profile update",
            )


    @staticmethod
    def upload_avatar(db: Session, current_user: User, file: UploadFile):
        ext = Path(file.filename).suffix.lower()
        if ext not in ALLOWED_EXTENSIONS:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Only JPG, JPEG, PNG, and GIF files are allowed",
            )

        file.file.seek(0, 2)
        file_size = file.file.tell()
        file.file.seek(0)

        if file_size > MAX_FILE_SIZE:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="File size must be less than 5MB",
            )

        unique_name = f"{current_user.id}_{uuid.uuid4()}{ext}"
        file_path = AVATAR_UPLOAD_DIR / unique_name

        try:
            # Delete old avatar
            if current_user.avatar_url:
                old_name = current_user.avatar_url.split("/")[-1]
                old_path = AVATAR_UPLOAD_DIR / old_name
                if old_path.exists():
                    old_path.unlink()

            with open(file_path, "wb") as buffer:
                shutil.copyfileobj(file.file, buffer)

            current_user.avatar_url = f"/static/avatars/{unique_name}"
            db.commit()
            db.refresh(current_user)
            return current_user

        except Exception:
            if file_path.exists():
                file_path.unlink()
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Failed to upload avatar",
            )


    @staticmethod
    def delete_avatar(db: Session, current_user: User):
        if current_user.avatar_url:
            filename = current_user.avatar_url.split("/")[-1]
            file_path = AVATAR_UPLOAD_DIR / filename
            if file_path.exists():
                file_path.unlink()

        current_user.avatar_url = None
        db.commit()
        db.refresh(current_user)

        return current_user