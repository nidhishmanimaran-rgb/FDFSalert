from pydantic import BaseModel
from typing import Optional, List

class WatchlistItemCreate(BaseModel):
    movie_title: str
    cinema_name: str
    target_date: str
    is_morning_show_only: bool = True

class WatchlistItemResponse(BaseModel):
    id: int
    movie_title: str
    cinema_name: str
    target_date: str
    is_morning_show_only: bool
    status: str
    booking_url: str
    last_checked_at: float
    is_alert_enabled: bool

    class Config:
        from_attributes = True

class AlertLogResponse(BaseModel):
    id: int
    watchlist_id: int
    movie_title: str
    cinema_name: str
    event_status: str
    timestamp: float
    booking_url: str
    triggered_via: str

    class Config:
        from_attributes = True

class DeviceTokenCreate(BaseModel):
    token: str
    platform: str = "ANDROID"

class HealthCheckResponse(BaseModel):
    status: str
    active_watchlist_count: int
    total_alerts_triggered: int
    uptime_seconds: float
