from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles
from app.database import engine
from app.database import Base
from .routes import register_routers
from app.core.seed import seed_categories
from contextlib import asynccontextmanager

@asynccontextmanager
async def lifespan(app: FastAPI):
    seed_categories()
    yield

app = FastAPI(
    title="Personal Finance Tracker API",
    description="Backend API for personal finance management application",
    version="1.0.0",
    lifespan=lifespan
)

Base.metadata.create_all(bind=engine)

app.mount("/static", StaticFiles(directory="static"), name="static")

register_routers(app)

@app.get("/")
def root():
    return {
        "message": "Equilibrio API",
        "version": "1.0.0",
        "docs": "/docs",
        "redoc": "/redoc"
    }

@app.get("/health")
def health_check():
    return {"status": "healthy"}