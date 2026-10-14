package com.example.myapplication

import com.example.myapplication.notification.NotificationHelper
import org.junit.Assert.*
import org.junit.Test

class NotificationPayloadTest {

    @Test
    fun testChannelConfiguration() {
        assertEquals("fdfs_alert_channel", NotificationHelper.CHANNEL_ID)
        assertEquals("FDFS Booking Alerts", NotificationHelper.CHANNEL_NAME)
        assertEquals(1001, NotificationHelper.NOTIFICATION_ID)
    }
}
