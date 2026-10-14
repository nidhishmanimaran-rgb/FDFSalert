import abc
import urllib.parse
from app.models import BookingStatusEnum

class SourceAdapter(abc.ABC):
    @abc.abstractmethod
    def fetch_booking_status(self, movie_title: str, cinema_name: str, target_date: str) -> tuple[str, str]:
        """Returns (BookingStatusEnum, booking_url)"""
        pass

class SimulationAdapter(SourceAdapter):
    """Simulation adapter that transitions UNOPENED -> OPEN_SOON -> OPEN for testing."""
    def __init__(self):
        self._simulation_counter = {}

    def fetch_booking_status(self, movie_title: str, cinema_name: str, target_date: str) -> tuple[str, str]:
        key = f"{movie_title}_{cinema_name}_{target_date}"
        count = self._simulation_counter.get(key, 0) + 1
        self._simulation_counter[key] = count

        encoded_title = urllib.parse.quote_plus(f"{movie_title} {cinema_name} booking")
        booking_url = f"https://www.google.com/search?q={encoded_title}"

        if count == 1:
            return (BookingStatusEnum.UNOPENED.value, booking_url)
        elif count == 2:
            return (BookingStatusEnum.OPEN_SOON.value, booking_url)
        else:
            return (BookingStatusEnum.OPEN.value, booking_url)

class AuthorizedSourceAdapter(SourceAdapter):
    """
    Authorized source adapter framework for BookMyShow / District / Cinema web sources.
    Respects access restrictions and returns UNKNOWN when unauthorized or unavailable.
    """
    def fetch_booking_status(self, movie_title: str, cinema_name: str, target_date: str) -> tuple[str, str]:
        # Return UNKNOWN if live credentials or permitted API endpoint is unconfigured
        encoded = urllib.parse.quote_plus(f"{movie_title} {cinema_name}")
        return (BookingStatusEnum.UNKNOWN.value, f"https://www.google.com/search?q={encoded}")
