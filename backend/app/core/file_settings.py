from pathlib import Path

AVATAR_UPLOAD_DIR = Path("static/avatars")
AVATAR_UPLOAD_DIR.mkdir(parents=True, exist_ok=True)

TRANSACTION_UPLOAD_DIR = Path("static/receipts")
TRANSACTION_UPLOAD_DIR.mkdir(parents=True, exist_ok=True)

GOAL_UPLOAD_DIR = Path("static/goals")
GOAL_UPLOAD_DIR.mkdir(parents=True, exist_ok=True)

ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif"}
MAX_FILE_SIZE = 5 * 1024 * 1024