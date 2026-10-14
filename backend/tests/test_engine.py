import pytest
from app.engine import BookingStatusEngine
from app.models import BookingStatusEnum

def test_unopened_to_open_triggers_alert():
    eval_res = BookingStatusEngine.evaluate_transition(
        previous_status=BookingStatusEnum.UNOPENED.value,
        fetched_status=BookingStatusEnum.OPEN.value,
        booking_url="https://example.com/book"
    )
    assert eval_res.should_trigger_alert is True
    assert eval_res.new_status == BookingStatusEnum.OPEN.value

def test_unopened_to_open_soon_triggers_alert():
    eval_res = BookingStatusEngine.evaluate_transition(
        previous_status=BookingStatusEnum.UNOPENED.value,
        fetched_status=BookingStatusEnum.OPEN_SOON.value,
        booking_url="https://example.com/book"
    )
    assert eval_res.should_trigger_alert is True
    assert eval_res.new_status == BookingStatusEnum.OPEN_SOON.value

def test_unknown_status_does_not_trigger_alert():
    eval_res = BookingStatusEngine.evaluate_transition(
        previous_status=BookingStatusEnum.UNOPENED.value,
        fetched_status=BookingStatusEnum.UNKNOWN.value,
        booking_url=""
    )
    assert eval_res.should_trigger_alert is False
    assert eval_res.new_status == BookingStatusEnum.UNOPENED.value

def test_duplicate_status_suppresses_alert():
    eval_res = BookingStatusEngine.evaluate_transition(
        previous_status=BookingStatusEnum.OPEN.value,
        fetched_status=BookingStatusEnum.OPEN.value,
        booking_url="https://example.com/book"
    )
    assert eval_res.should_trigger_alert is False
