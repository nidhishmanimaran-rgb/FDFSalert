package com.example.myapplication.engine

import com.example.myapplication.data.db.BookingStatus

data class StatusEvaluationResult(
    val previousStatus: BookingStatus,
    val newStatus: BookingStatus,
    val shouldTriggerAlert: Boolean,
    val bookingUrl: String,
    val reason: String
)

object BookingStatusEngine {

    /**
     * Evaluates status transition and returns whether an FDFS alert should be triggered.
     * Prevents false alerts on UNKNOWN or CLOSED, and prevents duplicate alerts.
     */
    fun evaluateTransition(
        previousStatus: BookingStatus,
        fetchedStatus: BookingStatus,
        bookingUrl: String
    ): StatusEvaluationResult {
        // UNKNOWN or error from booking source must NEVER trigger a false opening alert
        if (fetchedStatus == BookingStatus.UNKNOWN) {
            return StatusEvaluationResult(
                previousStatus = previousStatus,
                newStatus = previousStatus, // preserve previous known status
                shouldTriggerAlert = false,
                bookingUrl = bookingUrl,
                reason = "Source returned UNKNOWN state - ignoring to prevent false alert."
            )
        }

        // Duplicate event check: if status has not changed, do not trigger duplicate alert
        if (fetchedStatus == previousStatus) {
            return StatusEvaluationResult(
                previousStatus = previousStatus,
                newStatus = fetchedStatus,
                shouldTriggerAlert = false,
                bookingUrl = bookingUrl,
                reason = "Status unchanged ($fetchedStatus) - suppressed duplicate alert."
            )
        }

        // Trigger alert only when status transitions to OPEN or OPEN_SOON from a non-open status
        val isOpening = (fetchedStatus == BookingStatus.OPEN || fetchedStatus == BookingStatus.OPEN_SOON) &&
                (previousStatus == BookingStatus.UNOPENED || previousStatus == BookingStatus.UNKNOWN || previousStatus == BookingStatus.CLOSED)

        return StatusEvaluationResult(
            previousStatus = previousStatus,
            newStatus = fetchedStatus,
            shouldTriggerAlert = isOpening,
            bookingUrl = bookingUrl,
            reason = if (isOpening) "FDFS Booking Opening Detected!" else "Status changed from $previousStatus to $fetchedStatus."
        )
    }

    fun isValidBookingUrl(url: String): Boolean {
        if (url.isBlank()) return false
        val lower = url.lowercase()
        return lower.startsWith("http://") || lower.startsWith("https://")
    }
}
