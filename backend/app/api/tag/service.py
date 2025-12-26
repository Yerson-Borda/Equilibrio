from sqlalchemy.orm import Session
from fastapi import HTTPException
from typing import List

from app.entities.tag import Tag
from app.api.tag.model import TagCreate, TagResponse

def create_tag(db: Session, user_id: int, data: TagCreate) -> TagResponse:
    # avoid duplicate tag names for same user (case-insensitive)
    existing = db.query(Tag).filter(
        Tag.user_id == user_id,
        Tag.name.ilike(data.name.strip())
    ).first()

    if existing:
        raise HTTPException(400, f"Tag with name '{data.name}' already exists")

    tag = Tag(
        name=data.name.strip(),
        user_id=user_id
    )

    db.add(tag)
    db.commit()
    db.refresh(tag)

    return TagResponse.from_orm(tag)


def get_user_tags(db: Session, user_id: int) -> List[TagResponse]:
    tags = db.query(Tag).filter(Tag.user_id == user_id).order_by(Tag.name.asc()).all()
    return [TagResponse.from_orm(t) for t in tags]


def delete_tag(db: Session, user_id: int, tag_id: int):
    tag = db.query(Tag).filter(Tag.id == tag_id, Tag.user_id == user_id).first()
    if not tag:
        raise HTTPException(404, "Tag not found")

    db.delete(tag)
    db.commit()
    return
