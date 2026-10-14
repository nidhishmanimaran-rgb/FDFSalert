import os
import logging
from typing import List

logger = logging.getLogger("FDFS_FCM")

# Firebase Admin SDK initialization
firebase_app = None
try:
    import firebase_admin
    from firebase_admin import credentials, messaging

    cred_path = os.getenv("GOOGLE_APPLICATION_CREDENTIALS", "service_account_key.json")
    if os.path.exists(cred_path):
        cred = credentials.Certificate(cred_path)
        firebase_app = firebase_admin.initialize_app(cred)
        logger.info(f"Firebase Admin SDK initialized successfully with credentials at: {cred_path}")
    else:
        logger.info(f"Firebase Admin credential file not found at '{cred_path}'. Operating in simulation mode.")
except Exception as e:
    logger.warning(f"Could not initialize Firebase Admin SDK: {e}. Operating in simulation mode.")

class FCMNotificationDispatcher:

    @staticmethod
    def send_push_notification(
        device_tokens: List[str],
        movie_title: str,
        cinema_name: str,
        booking_url: str,
        status: str
    ) -> int:
        """
        Dispatches FCM push notifications to registered device tokens.
        Uses Firebase Admin SDK if initialized; otherwise records simulation logs.
        """
        if not device_tokens:
            logger.info("No registered device tokens to dispatch push notification.")
            return 0

        # Filter out empty/null tokens
        valid_tokens = [t for t in device_tokens if t and t.strip()]
        if not valid_tokens:
            return 0

        if firebase_app:
            try:
                from firebase_admin import messaging

                message = messaging.MulticastMessage(
                    data={
                        "movie_title": movie_title,
                        "cinema_name": cinema_name,
                        "booking_url": booking_url,
                        "status": status,
                        "alert_type": "FDFS_MORNING_SHOW"
                    },
                    notification=messaging.Notification(
                        title=f"🚨 FDFS BOOKING OPEN: {movie_title}!",
                        body=f"Morning show tickets are now OPEN at {cinema_name}. Tap to book!"
                    ),
                    tokens=valid_tokens
                )

                response = messaging.send_each_for_multicast(message)
                logger.info(f"FCM Multicast sent: {response.success_count} success, {response.failure_count} failures.")
                return response.success_count
            except Exception as e:
                logger.error(f"Error sending FCM multicast notification: {e}")
                return 0
        else:
            # Simulation / Mock Mode
            payload = {
                "movie_title": movie_title,
                "cinema_name": cinema_name,
                "booking_url": booking_url,
                "status": status,
                "alert_type": "FDFS_MORNING_SHOW"
            }
            logger.info(f"[SIMULATION] Dispatched FCM Push Notification payload: {payload} to {len(valid_tokens)} tokens.")
            return len(valid_tokens)
