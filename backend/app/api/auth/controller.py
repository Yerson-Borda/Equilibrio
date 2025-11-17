from fastapi import APIRouter, Depends, File, UploadFile, status
from fastapi.security import HTTPAuthorizationCredentials
from sqlalchemy.orm import Session

from app.database import get_db
from app.entities.user import User
from app.api.auth.model import UserCreate, UserUpdate, UserResponse, Token
from app.auth import get_current_user, security
from app.api.auth.service import AuthService

router = APIRouter()

@router.post("/logout")
def logout(
    current_user: User = Depends(get_current_user),
    credentials: HTTPAuthorizationCredentials = Depends(security)
):
    """Logout user by blacklisting token"""
    return AuthService.logout(credentials.credentials)


@router.post("/register", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
def register(user_data: UserCreate, db: Session = Depends(get_db)):
    """Register a new user"""
    return AuthService.register(db, user_data)


@router.post("/login", response_model=Token)
def login(email: str, password: str, db: Session = Depends(get_db)):
    """Authenticate user"""
    token = AuthService.login(db, email, password)
    return {"access_token": token, "token_type": "bearer"}


@router.get("/me", response_model=UserResponse)
def get_me(current_user: User = Depends(get_current_user)):
    """Return current user basic info"""
    return current_user


@router.get("/me/detailed", response_model=dict)
def get_me_detailed(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Return user, wallet stats, and transaction stats"""
    return AuthService.get_detailed_user_info(db, current_user)


@router.put("/me", response_model=UserResponse)
def update_user(
    user_data: UserUpdate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Update current user profile"""
    return AuthService.update_user(db, current_user, user_data)


@router.post("/me/avatar", response_model=UserResponse)
async def upload_avatar(
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Upload or replace user avatar image"""
    return AuthService.upload_avatar(db, current_user, file)


@router.delete("/me/avatar", response_model=UserResponse)
def delete_avatar(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Delete user avatar"""
    return AuthService.delete_avatar(db, current_user)