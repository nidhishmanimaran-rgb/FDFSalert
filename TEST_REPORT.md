# FDFS Alert — Test Report

## Test Execution Summary

### 1. Android Static Analysis (`.\gradlew.bat lint`)
- Status: PASSED (Exit Code: 0)
- Total Errors: 0
- Report Location: `app/build/reports/lint-results-debug.html`

### 2. Android Unit Tests (`.\gradlew.bat test`)
- Status: PASSED (Exit Code: 0)
- Total Tests: 7
- Passed: 7
- Failed: 0
- Test Suites:
  - `BookingStatusEngineTest`:
    - `testUnopenedToOpenTriggersAlert` — PASSED
    - `testUnopenedToOpenSoonTriggersAlert` — PASSED
    - `testUnknownStatusDoesNotTriggerAlertAndPreservesStatus` — PASSED
    - `testDuplicateOpenStatusSuppressesAlert` — PASSED
    - `testBookingUrlValidation` — PASSED
  - `NotificationPayloadTest`:
    - `testChannelConfiguration` — PASSED
  - `ExampleUnitTest`:
    - `addition_isCorrect` — PASSED

### 3. Python Backend Pytest Suite (`python -m pytest -v`)
- Status: PASSED (Exit Code: 0)
- Total Tests: 8
- Passed: 8
- Failed: 0
- Tests:
  - `tests/test_api.py::test_health_check_endpoint` — PASSED
  - `tests/test_api.py::test_watchlist_crud_api` — PASSED
  - `tests/test_api.py::test_device_registration_api` — PASSED
  - `tests/test_api.py::test_trigger_test_alert_api` — PASSED
  - `tests/test_engine.py::test_unopened_to_open_triggers_alert` — PASSED
  - `tests/test_engine.py::test_unopened_to_open_soon_triggers_alert` — PASSED
  - `tests/test_engine.py::test_unknown_status_does_not_trigger_alert` — PASSED
  - `tests/test_engine.py::test_duplicate_status_suppresses_alert` — PASSED

### 4. Real Local HTTP Server Verification (`live_server_test.py`)
- `GET /health`: 200 OK (`status: HEALTHY`)
- `POST /api/watchlist`: 200 OK (`Game Changer (FDFS)`)
- `GET /api/watchlist`: 200 OK (`Watchlist Count: 1`)
- `POST /api/devices/register`: 200 OK (`Device token registered successfully`)
- `POST /api/alerts/test`: 200 OK (`Test alert dispatched`)
- `GET /api/alerts`: 200 OK (`Alert Log Count: 2`)
