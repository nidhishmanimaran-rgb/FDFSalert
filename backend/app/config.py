import os
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    APP_NAME: str = "FDFS Alert API"
    DATABASE_URL: str = "sqlite:///./fdfs_alert.db"
    SECRET_KEY: str = "fdfs_secret_key_change_in_production"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 1440
    POLL_INTERVAL_SECONDS: int = 15

    class Config:
        env_file = ".env"

settings = Settings()
