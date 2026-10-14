import time
import logging
import threading
from sqlalchemy.orm import Session
from app.db import SessionLocal
from app.models import WatchlistItem, AlertLog, DeviceToken
from app.engine import BookingStatusEngine
from app.adapters import SimulationAdapter, AuthorizedSourceAdapter
from app.fcm import FCMNotificationDispatcher

logger = logging.getLogger("FDFS_Worker")

class MonitoringWorker:
    def __init__(self, interval_seconds: int = 15):
        self.interval_seconds = interval_seconds
        self.is_running = False
        self.thread = None
        self.adapter = SimulationAdapter()

    fun_start = None

    def start(self):
        if self.is_running:
            return
        self.is_running = True
        self.thread = threading.Thread(target=self._run_loop, daemon=True)
        self.thread.start()
        logger.info("FDFS Monitoring Worker started.")

    def stop(self):
        self.is_running = False
        if self.thread and self.thread.is_alive():
            self.thread.join(timeout=2.0)
        logger.info("FDFS Monitoring Worker stopped.")

    def run_once(self):
        db: Session = SessionLocal()
        try:
            active_items = db.query(WatchlistItem).filter(WatchlistItem.is_alert_enabled == True).all()
            tokens = [t.token for t in db.query(DeviceToken).all()]

            for item in active_items:
                fetched_status, booking_url = self.adapter.fetch_booking_status(
                    movie_title=item.movie_title,
                    cinema_name=item.cinema_name,
                    target_date=item.target_date
                )

                eval_result = BookingStatusEngine.evaluate_transition(
                    previous_status=item.status,
                    fetched_status=fetched_status,
                    booking_url=booking_url
                )

                item.status = eval_result.new_status
                item.booking_url = eval_result.booking_url
                item.last_checked_at = time.time()
                db.commit()

                if eval_result.should_trigger_alert:
                    alert_log = AlertLog(
                        watchlist_id=item.id,
                        movie_title=item.movie_title,
                        cinema_name=item.cinema_name,
                        event_status=eval_result.new_status,
                        timestamp=time.time(),
                        booking_url=eval_result.booking_url,
                        triggered_via="WORKER_MONITOR"
                    )
                    db.add(alert_log)
                    db.commit()

                    FCMNotificationDispatcher.send_push_notification(
                        device_tokens=tokens,
                        movie_title=item.movie_title,
                        cinema_name=item.cinema_name,
                        booking_url=eval_result.booking_url,
                        status=eval_result.new_status
                    )
        except Exception as e:
            logger.error(f"Error in worker run_once: {e}")
            db.rollback()
        finally:
            db.close()

    def _run_loop(self):
        while self.is_running:
            self.run_once()
            time.sleep(self.interval_seconds)

monitoring_worker = MonitoringWorker(interval_seconds=15)
