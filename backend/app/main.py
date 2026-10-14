import time
from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from typing import List

from app.db import Base, engine, get_db
from app.models import WatchlistItem, AlertLog, DeviceToken
from app.schemas import (
    WatchlistItemCreate,
    WatchlistItemResponse,
    AlertLogResponse,
    DeviceTokenCreate,
    HealthCheckResponse
)
from app.worker import monitoring_worker
from app.fcm import FCMNotificationDispatcher

# Initialize database tables
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="FDFS Alert Backend API",
    description="Backend API and Monitoring Worker for First Day First Show Movie Booking Alerts",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

START_TIME = time.time()

@app.on_event("startup")
def startup_event():
    monitoring_worker.start()

@app.on_event("shutdown")
def shutdown_event():
    monitoring_worker.stop()

@app.get("/health", response_model=HealthCheckResponse)
def health_check(db: Session = Depends(get_db)):
    active_watchlist = db.query(WatchlistItem).filter(WatchlistItem.is_alert_enabled == True).count()
    total_alerts = db.query(AlertLog).count()
    uptime = time.time() - START_TIME
    return HealthCheckResponse(
        status="HEALTHY",
        active_watchlist_count=active_watchlist,
        total_alerts_triggered=total_alerts,
        uptime_seconds=uptime
    )

@app.get("/api/watchlist", response_model=List[WatchlistItemResponse])
def get_watchlist(db: Session = Depends(get_db)):
    return db.query(WatchlistItem).order_by(WatchlistItem.id.desc()).all()

@app.post("/api/watchlist", response_model=WatchlistItemResponse)
def create_watchlist_item(item: WatchlistItemCreate, db: Session = Depends(get_db)):
    db_item = WatchlistItem(
        movie_title=item.movie_title,
        cinema_name=item.cinema_name,
        target_date=item.target_date,
        is_morning_show_only=item.is_morning_show_only,
        status="UNOPENED",
        is_alert_enabled=True,
        last_checked_at=time.time()
    )
    db.add(db_item)
    db.commit()
    db.refresh(db_item)
    return db_item

@app.delete("/api/watchlist/{item_id}")
def delete_watchlist_item(item_id: int, db: Session = Depends(get_db)):
    db_item = db.query(WatchlistItem).filter(WatchlistItem.id == item_id).first()
    if not db_item:
        raise HTTPException(status_code=404, detail="Watchlist item not found")
    db.delete(db_item)
    db.commit()
    return {"message": f"Deleted watchlist item {item_id}"}

@app.get("/api/alerts", response_model=List[AlertLogResponse])
def get_alert_history(db: Session = Depends(get_db)):
    return db.query(AlertLog).order_by(AlertLog.timestamp.desc()).all()

@app.post("/api/devices/register")
def register_device(device: DeviceTokenCreate, db: Session = Depends(get_db)):
    existing = db.query(DeviceToken).filter(DeviceToken.token == device.token).first()
    if not existing:
        db_token = DeviceToken(token=device.token, platform=device.platform)
        db.add(db_token)
        db.commit()
        return {"message": "Device token registered successfully"}
    return {"message": "Device token already registered"}

@app.post("/api/alerts/test")
def trigger_test_alert(db: Session = Depends(get_db)):
    tokens = [t.token for t in db.query(DeviceToken).all()]
    sent_count = FCMNotificationDispatcher.send_push_notification(
        device_tokens=tokens if tokens else ["simulated_fcm_token"],
        movie_title="Avatar 3: Fire & Ash (FDFS)",
        cinema_name="PVR IMAX Forum Mall",
        booking_url="https://www.google.com/search?q=Avatar+3+booking",
        status="OPEN"
    )
    alert_log = AlertLog(
        watchlist_id=0,
        movie_title="Avatar 3: Fire & Ash (FDFS)",
        cinema_name="PVR IMAX Forum Mall",
        event_status="OPEN",
        timestamp=time.time(),
        booking_url="https://www.google.com/search?q=Avatar+3+booking",
        triggered_via="API_TEST"
    )
    db.add(alert_log)
    db.commit()
    return {"message": "Test alert dispatched", "dispatched_count": sent_count}
