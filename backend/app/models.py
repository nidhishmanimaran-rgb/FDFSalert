import enum
import time
from sqlalchemy import Column, Integer, String, Boolean, Enum, Float, ForeignKey, DateTime
from sqlalchemy.orm import relationship
from app.db import Base

class BookingStatusEnum(str, enum.Enum):
    UNOPENED = "UNOPENED"
    OPEN_SOON = "OPEN_SOON"
    OPEN = "OPEN"
    UNKNOWN = "UNKNOWN"
    CLOSED = "CLOSED"

class Movie(Base):
    __tablename__ = "movies"
    id = Column(Integer, primary_key=True, index=True)
    title = Column(String, nullable=False, index=True)
    language = Column(String, default="Tamil")
    release_date = Column(String, nullable=False)
    created_at = Column(Float, default=time.time)

class Cinema(Base):
    __tablename__ = "cinemas"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False, index=True)
    city = Column(String, nullable=False)
    location = Column(String, nullable=False)
    format = Column(String, default="IMAX 3D")

class WatchlistItem(Base):
    __tablename__ = "watchlist"
    id = Column(Integer, primary_key=True, index=True)
    movie_title = Column(String, nullable=False)
    cinema_name = Column(String, nullable=False)
    target_date = Column(String, nullable=False)
    is_morning_show_only = Column(Boolean, default=True)
    status = Column(String, default="UNOPENED")
    booking_url = Column(String, default="")
    last_checked_at = Column(Float, default=time.time)
    is_alert_enabled = Column(Boolean, default=True)

class AlertLog(Base):
    __tablename__ = "alert_logs"
    id = Column(Integer, primary_key=True, index=True)
    watchlist_id = Column(Integer, nullable=False)
    movie_title = Column(String, nullable=False)
    cinema_name = Column(String, nullable=False)
    event_status = Column(String, nullable=False)
    timestamp = Column(Float, default=time.time)
    booking_url = Column(String, default="")
    triggered_via = Column(String, default="WORKER_MONITOR")

class DeviceToken(Base):
    __tablename__ = "device_tokens"
    id = Column(Integer, primary_key=True, index=True)
    token = Column(String, unique=True, nullable=False, index=True)
    platform = Column(String, default="ANDROID")
    registered_at = Column(Float, default=time.time)
