import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_health_check_endpoint():
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "HEALTHY"
    assert "active_watchlist_count" in data
    assert "total_alerts_triggered" in data

def test_watchlist_crud_api():
    # Create watchlist item
    post_res = client.post("/api/watchlist", json={
        "movie_title": "Coolie (FDFS)",
        "cinema_name": "PVR Forum",
        "target_date": "2026-11-20",
        "is_morning_show_only": True
    })
    assert post_res.status_code == 200
    created = post_res.json()
    assert created["movie_title"] == "Coolie (FDFS)"
    item_id = created["id"]

    # Get watchlist
    get_res = client.get("/api/watchlist")
    assert get_res.status_code == 200
    items = get_res.json()
    assert len(items) >= 1

    # Delete item
    del_res = client.delete(f"/api/watchlist/{item_id}")
    assert del_res.status_code == 200

def test_device_registration_api():
    res = client.post("/api/devices/register", json={
        "token": "test_fcm_device_token_123",
        "platform": "ANDROID"
    })
    assert res.status_code == 200

def test_trigger_test_alert_api():
    res = client.post("/api/alerts/test")
    assert res.status_code == 200
    assert "Test alert dispatched" in res.json()["message"]
