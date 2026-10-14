from app.models import BookingStatusEnum

class EvaluationResult:
    def __init__(self, previous_status: str, new_status: str, should_trigger_alert: bool, booking_url: str, reason: str):
        self.previous_status = previous_status
        self.new_status = new_status
        self.should_trigger_alert = should_trigger_alert
        self.booking_url = booking_url
        self.reason = reason

class BookingStatusEngine:

    @staticmethod
    def evaluate_transition(previous_status: str, fetched_status: str, booking_url: str) -> EvaluationResult:
        # Source failure or UNKNOWN status must never trigger false alerts
        if fetched_status == BookingStatusEnum.UNKNOWN.value:
            return EvaluationResult(
                previous_status=previous_status,
                new_status=previous_status,
                should_trigger_alert=False,
                booking_url=booking_url,
                reason="UNKNOWN state - preserving previous known state without alert."
            )

        # Duplicate event check: if status is unchanged, suppress duplicate alert
        if fetched_status == previous_status:
            return EvaluationResult(
                previous_status=previous_status,
                new_status=fetched_status,
                should_trigger_alert=False,
                booking_url=booking_url,
                reason=f"Status unchanged ({fetched_status}) - duplicate alert suppressed."
            )

        # Trigger alert only when status transitions to OPEN or OPEN_SOON from non-open
        is_opening = (fetched_status in [BookingStatusEnum.OPEN.value, BookingStatusEnum.OPEN_SOON.value]) and \
                     (previous_status in [BookingStatusEnum.UNOPENED.value, BookingStatusEnum.UNKNOWN.value, BookingStatusEnum.CLOSED.value])

        return EvaluationResult(
            previous_status=previous_status,
            new_status=fetched_status,
            should_trigger_alert=is_opening,
            booking_url=booking_url,
            reason="FDFS Booking Opening Detected!" if is_opening else f"Status transitioned to {fetched_status}"
        )
