from pathlib import Path
from sqlalchemy.orm import Session
from sqlalchemy.exc import IntegrityError
from fastapi import HTTPException, status
from app.entities.user import User
from app.api.auth.model import UserCreate
from app.core.security import (
    get_password_hash,
    verify_password,
    create_access_token,
    token_blacklist
)

class AuthService:

    @staticmethod
    def register(db: Session, user_data: UserCreate):
        """Create a new user account"""
        try:
            existing_user = db.query(User).filter(User.email == user_data.email).first()
            if existing_user:
                raise HTTPException(
                    status_code=status.HTTP_409_CONFLICT,
                    detail="Email already registered",
                )

            user = User(
                email=user_data.email,
                hashed_password=get_password_hash(user_data.password),
                full_name=user_data.full_name,
                phone_number=user_data.phone_number,
                date_of_birth=user_data.date_of_birth,
                default_currency=user_data.default_currency,
            )

            db.add(user)
            db.commit()
            db.refresh(user)
            return user

        except HTTPException:
            raise

        except IntegrityError as e:
            db.rollback()
            if "unique" in str(e).lower() or "duplicate" in str(e).lower():
                raise HTTPException(
                    status_code=status.HTTP_409_CONFLICT,
                    detail="Email already registered",
                )
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid data provided",
            )

        except Exception:
            db.rollback()
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Internal server error during registration",
            )


    @staticmethod
    def login(db: Session, email: str, password: str):
        """Authenticate user and return JWT"""
        user = db.query(User).filter(User.email == email).first()

        if not user or not verify_password(password, user.hashed_password):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Incorrect email or password",
            )

        token = create_access_token({"sub": user.email})
        return token


    @staticmethod
    def logout(token: str):
        """Blacklist token to invalidate it"""
        token_blacklist.add(token)
        return {"message": "Successfully logged out"}