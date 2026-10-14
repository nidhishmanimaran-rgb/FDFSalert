import time
import threading
import uvicorn
import httpx
from app.main import app

def run_server():
    uvicorn.run(app, host="127.0.0.1", port=8005, log_level="warning")

if __name__ == "__main__":
    # Start uvicorn server in background thread
    server_thread = threading.Thread(target=run_server, daemon=True)
    server_thread.start()
    time.sleep(2) # Give server time to bind and listen

    base_url = "http://127.0.0.1:8005"
    client = httpx.Client(base_url=base_url)

    print("--- 1. Testing GET /health ---")
    health_res = client.get("/health")
    print(f"Status Code: {health_res.status_code}, Response: {health_res.json()}")
    assert health_res.status_code == 200

    print("\n--- 2. Testing POST /api/watchlist ---")
    watchlist_res = client.post("/api/watchlist", json={
        "movie_title": "Game Changer (FDFS)",
        "cinema_name": "PVR Forum Mall",
        "target_date": "2026-11-25",
        "is_morning_show_only": True
    })
    print(f"Status Code: {watchlist_res.status_code}, Response: {watchlist_res.json()}")
    assert watchlist_res.status_code == 200

    print("\n--- 3. Testing GET /api/watchlist ---")
    get_w_res = client.get("/api/watchlist")
    print(f"Status Code: {get_w_res.status_code}, Watchlist Count: {len(get_w_res.json())}")
    assert get_w_res.status_code == 200

    print("\n--- 4. Testing POST /api/devices/register ---")
    device_res = client.post("/api/devices/register", json={
        "token": "live_test_device_fcm_token_999",
        "platform": "ANDROID"
    })
    print(f"Status Code: {device_res.status_code}, Response: {device_res.json()}")
    assert device_res.status_code == 200

    print("\n--- 5. Testing POST /api/alerts/test ---")
    test_alert_res = client.post("/api/alerts/test")
    print(f"Status Code: {test_alert_res.status_code}, Response: {test_alert_res.json()}")
    assert test_alert_res.status_code == 200

    print("\n--- 6. Testing GET /api/alerts ---")
    alerts_res = client.get("/api/alerts")
    print(f"Status Code: {alerts_res.status_code}, Alert Log Count: {len(alerts_res.json())}")
    assert alerts_res.status_code == 200

    print("\nALL REAL LOCAL BACKEND HTTP ENDPOINTS VERIFIED SUCCESSFULLY!")
