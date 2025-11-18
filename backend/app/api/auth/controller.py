from fastapi import APIRouter, Depends, status
from fastapi.security import HTTPAuthorizationCredentials
from sqlalchemy.orm import Session
from app.database import get_db
from app.entities.user import User
from app.api.auth.model import UserCreate, UserResponse, Token
from app.core.auth import get_current_user, security
from app.api.auth.service import AuthService

router = APIRouter()

@router.post("/register", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
def register(user_data: UserCreate, db: Session = Depends(get_db)):
    return AuthService.register(db, user_data)


@router.post("/login", response_model=Token)
def login(email: str, password: str, db: Session = Depends(get_db)):
    token = AuthService.login(db, email, password)
    return {"access_token": token, "token_type": "bearer"}


@router.post("/logout")
def logout(
    current_user: User = Depends(get_current_user),
    credentials: HTTPAuthorizationCredentials = Depends(security)
):
    return AuthService.logout(credentials.credentials)