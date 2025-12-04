from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session
from typing import List

from app.database import get_db
from app.core.auth import get_current_user
from app.entities.user import User
from app.api.tag.model import TagCreate, TagResponse
from app.api.tag.service import create_tag, get_user_tags, delete_tag

router = APIRouter(prefix="/tags", tags=["tags"])

@router.post("/", response_model=TagResponse, status_code=status.HTTP_201_CREATED)
def create_tag_endpoint(
    data: TagCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return create_tag(db, current_user.id, data)

@router.get("/", response_model=List[TagResponse])
def get_user_tags_endpoint(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    return get_user_tags(db, current_user.id)

@router.delete("/{tag_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_tag_endpoint(
    tag_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    delete_tag(db, current_user.id, tag_id)
    return
