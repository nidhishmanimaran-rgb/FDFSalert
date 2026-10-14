package com.example.myapplication

import com.example.myapplication.data.db.BookingStatus
import com.example.myapplication.engine.BookingStatusEngine
import org.junit.Assert.*
import org.junit.Test

class BookingStatusEngineTest {

    @Test
    fun testUnopenedToOpenTriggersAlert() {
        val result = BookingStatusEngine.evaluateTransition(
            previousStatus = BookingStatus.UNOPENED,
            fetchedStatus = BookingStatus.OPEN,
            bookingUrl = "https://example.com/booking/movie1"
        )
        assertTrue(result.shouldTriggerAlert)
        assertEquals(BookingStatus.OPEN, result.newStatus)
    }

    @Test
    fun testUnopenedToOpenSoonTriggersAlert() {
        val result = BookingStatusEngine.evaluateTransition(
            previousStatus = BookingStatus.UNOPENED,
            fetchedStatus = BookingStatus.OPEN_SOON,
            bookingUrl = "https://example.com/booking/movie1"
        )
        assertTrue(result.shouldTriggerAlert)
        assertEquals(BookingStatus.OPEN_SOON, result.newStatus)
    }

    @Test
    fun testUnknownStatusDoesNotTriggerAlertAndPreservesStatus() {
        val result = BookingStatusEngine.evaluateTransition(
            previousStatus = BookingStatus.UNOPENED,
            fetchedStatus = BookingStatus.UNKNOWN,
            bookingUrl = ""
        )
        assertFalse(result.shouldTriggerAlert)
        assertEquals(BookingStatus.UNOPENED, result.newStatus)
    }

    @Test
    fun testDuplicateOpenStatusSuppressesAlert() {
        val result = BookingStatusEngine.evaluateTransition(
            previousStatus = BookingStatus.OPEN,
            fetchedStatus = BookingStatus.OPEN,
            bookingUrl = "https://example.com/booking/movie1"
        )
        assertFalse(result.shouldTriggerAlert)
    }

    @Test
    fun testBookingUrlValidation() {
        assertTrue(BookingStatusEngine.isValidBookingUrl("https://bookmyshow.com/movie"))
        assertTrue(BookingStatusEngine.isValidBookingUrl("http://district.in/show"))
        assertFalse(BookingStatusEngine.isValidBookingUrl(""))
        assertFalse(BookingStatusEngine.isValidBookingUrl("invalid_url"))
    }
}
