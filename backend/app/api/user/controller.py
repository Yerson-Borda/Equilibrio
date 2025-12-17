from fastapi import APIRouter, Depends, File, UploadFile
from sqlalchemy.orm import Session
from app.database import get_db
from app.entities.user import User
from app.api.auth.model import UserUpdate, UserResponse
from app.core.auth import get_current_user
from app.api.user.service import UserService

router = APIRouter()

@router.get("/me", response_model=UserResponse)
def get_me(current_user: User = Depends(get_current_user)):
    return current_user


@router.get("/me/detailed", response_model=dict)
def get_me_detailed(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return UserService.get_detailed_user_info(db, current_user)


@router.put("/me", response_model=UserResponse)
def update_user(
    user_data: UserUpdate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return UserService.update_user(db, current_user, user_data)


@router.post("/me/avatar", response_model=UserResponse)
async def upload_avatar(
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return UserService.upload_avatar(db, current_user, file)


@router.delete("/me/avatar", response_model=UserResponse)
def delete_avatar(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return UserService.delete_avatar(db, current_user)