from fastapi import APIRouter, Depends, HTTPException, status, UploadFile, File
from sqlalchemy.orm import Session
from typing import Optional
import shutil
import os
from pathlib import Path
import uuid
from app.database import get_db
from app.models.models import User, Wallet, Transaction, TransactionType
from app.schemas.schemas import UserCreate, UserResponse, UserUpdate, Token
from app.core.security import get_password_hash, verify_password, create_access_token, token_blacklist
from app.auth import get_current_user, security
from fastapi.security import HTTPAuthorizationCredentials
from sqlalchemy.exc import IntegrityError

router = APIRouter()

# Avatar config
AVATAR_UPLOAD_DIR = Path("static/avatars")
AVATAR_UPLOAD_DIR.mkdir(parents=True, exist_ok=True)
ALLOWED_EXTENSIONS = {'.jpg', '.jpeg', '.png', '.gif'}
MAX_FILE_SIZE = 5 * 1024 * 1024  # 5MB

@router.post("/logout", operation_id="logout_user") 
def logout(
    current_user: User = Depends(get_current_user),
    credentials: HTTPAuthorizationCredentials = Depends(security)
):
    """Logout user by blacklisting token"""
    token = credentials.credentials
    token_blacklist.add(token)
    
    return {"message": "Successfully logged out"}

@router.post("/register", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
def register(user_data: UserCreate, db: Session = Depends(get_db)):
    try:
        # Check if user already exists
        existing_user = db.query(User).filter(User.email == user_data.email).first()
        if existing_user:
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Email already registered"
            )
        
        hashed_password = get_password_hash(user_data.password)
        
        user = User(
            email=user_data.email,
            hashed_password=hashed_password,
            full_name=user_data.full_name,
            phone_number=user_data.phone_number,
            date_of_birth=user_data.date_of_birth,
            default_currency=user_data.default_currency
            # Note: avatar_url is not set during registration
        )
        
        db.add(user)
        db.commit()
        db.refresh(user)
        
        return user
        
    except HTTPException:
        raise
    except IntegrityError as e:
        db.rollback()
        if "unique constraint" in str(e).lower() or "duplicate key" in str(e).lower():
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Email already registered"
            )
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid data provided"
        )
    except Exception as e:
        db.rollback()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Internal server error during registration"
        )

@router.post("/login", response_model=Token)
def login(email: str, password: str, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.email == email).first()
    if not user or not verify_password(password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect email or password"
        )
    
    access_token = create_access_token(data={"sub": user.email})
    return {"access_token": access_token, "token_type": "bearer"}

@router.get("/me", response_model=UserResponse)
def get_current_user_info(current_user: User = Depends(get_current_user)):
    return current_user

@router.get("/me/detailed", response_model=dict)
def get_detailed_user_info(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Get detailed user information with wallet and transaction counts"""
    
    wallet_count = db.query(Wallet).filter(Wallet.user_id == current_user.id).count()
    
    expense_count = db.query(Transaction).filter(
        Transaction.user_id == current_user.id,
        Transaction.type == TransactionType.EXPENSE
    ).count()
    
    income_count = db.query(Transaction).filter(
        Transaction.user_id == current_user.id,
        Transaction.type == TransactionType.INCOME
    ).count()
    
    return {
        "user": {
            "id": current_user.id,
            "email": current_user.email,
            "full_name": current_user.full_name,
            "phone_number": current_user.phone_number,
            "date_of_birth": current_user.date_of_birth,
            "avatar_url": current_user.avatar_url,
            "default_currency": current_user.default_currency,
            "created_at": current_user.created_at
        },
        "stats": {
            "wallet_count": wallet_count,
            "total_transactions": expense_count + income_count,
            "expense_count": expense_count,
            "income_count": income_count
        }
    }

@router.put("/me", response_model=UserResponse)
def update_user_info(
    user_data: UserUpdate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Update user information - all fields optional"""
    try:
        if user_data.email is not None:
            existing_user = db.query(User).filter(
                User.email == user_data.email,
                User.id != current_user.id
            ).first()
            if existing_user:
                raise HTTPException(
                    status_code=status.HTTP_409_CONFLICT,
                    detail="Email already registered by another user"
                )
            current_user.email = user_data.email
        
        if user_data.full_name is not None:
            current_user.full_name = user_data.full_name
        
        if user_data.phone_number is not None:
            current_user.phone_number = user_data.phone_number
        
        if user_data.date_of_birth is not None:
            current_user.date_of_birth = user_data.date_of_birth
        
        if user_data.avatar_url is not None:
            current_user.avatar_url = user_data.avatar_url
        
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
        if "unique constraint" in str(e).lower() or "duplicate key" in str(e).lower():
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Email already registered by another user"
            )
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid data provided"
        )
    except Exception as e:
        db.rollback()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Internal server error during profile update"
        )

@router.post("/me/avatar", response_model=UserResponse)
async def upload_avatar(
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Upload user avatar image
    """
    file_extension = Path(file.filename).suffix.lower()
    if file_extension not in ALLOWED_EXTENSIONS:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Only JPG, JPEG, PNG, and GIF files are allowed"
        )
    
    file.file.seek(0, 2)
    file_size = file.file.tell()
    file.file.seek(0)
    
    if file_size > MAX_FILE_SIZE:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="File size must be less than 5MB"
        )
    
    unique_filename = f"{current_user.id}_{uuid.uuid4()}{file_extension}"
    file_path = AVATAR_UPLOAD_DIR / unique_filename
    
    try:
        if current_user.avatar_url:
            old_filename = current_user.avatar_url.split("/")[-1]
            old_file_path = AVATAR_UPLOAD_DIR / old_filename
            if old_file_path.exists():
                old_file_path.unlink()
        
        with open(file_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)
        
        avatar_url = f"/static/avatars/{unique_filename}"
        current_user.avatar_url = avatar_url
        db.commit()
        db.refresh(current_user)
        
        return current_user
        
    except Exception as e:
        if file_path.exists():
            file_path.unlink()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to upload avatar"
        )

@router.delete("/me/avatar", response_model=UserResponse)
def delete_avatar(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Delete user avatar
    """
    if current_user.avatar_url:
        filename = current_user.avatar_url.split("/")[-1]
        file_path = AVATAR_UPLOAD_DIR / filename
        if file_path.exists():
            file_path.unlink()
    
    current_user.avatar_url = None
    db.commit()
    db.refresh(current_user)
    
    return current_user