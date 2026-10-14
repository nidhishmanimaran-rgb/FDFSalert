package com.example.myapplication.fcm

import android.content.Context
import android.util.Log
import com.example.myapplication.data.db.AlertHistoryEntity
import com.example.myapplication.data.db.AppDatabase
import com.example.myapplication.data.db.BookingStatus
import com.example.myapplication.notification.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class FDFSFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token registered: $token")
        registerTokenWithBackend(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            handleRemoteMessagePayload(applicationContext, data)
        }
    }

    companion object {
        private const val TAG = "FDFS_FCM"

        fun registerTokenWithBackend(context: Context, token: String, backendUrlStr: String = "http://10.0.2.2:8000") {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val settings = db.appSettingsDao().getSettings()
                    val targetUrl = settings?.backendUrl?.ifBlank { backendUrlStr } ?: backendUrlStr
                    val endpoint = URL("$targetUrl/api/devices/register")

                    val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        setRequestProperty("Content-Type", "application/json")
                        doOutput = true
                        connectTimeout = 5000
                        readTimeout = 5000
                    }

                    val jsonInputString = "{\"token\": \"$token\", \"platform\": \"ANDROID\"}"
                    conn.outputStream.use { os ->
                        val input = jsonInputString.toByteArray(Charsets.UTF_8)
                        os.write(input, 0, input.size)
                    }

                    val responseCode = conn.responseCode
                    Log.d(TAG, "Backend token registration response code: $responseCode")
                    conn.disconnect()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to register FCM token with backend: ${e.message}")
                }
            }
        }

        fun handleRemoteMessagePayload(context: Context, data: Map<String, String>) {
            val movieTitle = data["movie_title"] ?: "Movie"
            val cinemaName = data["cinema_name"] ?: "Cinema"
            val bookingUrl = data["booking_url"] ?: ""
            val statusStr = data["status"] ?: "OPEN"

            Log.d(TAG, "Received FDFS alert payload: $data")

            NotificationHelper.triggerBookingAlert(
                context = context,
                movieTitle = movieTitle,
                cinemaName = cinemaName,
                bookingUrl = bookingUrl,
                vibrationEnabled = true
            )

            val status = try {
                BookingStatus.valueOf(statusStr)
            } catch (_: Exception) {
                BookingStatus.OPEN
            }

            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getInstance(context)
                db.alertHistoryDao().insertHistory(
                    AlertHistoryEntity(
                        watchlistItemId = data["watchlist_id"]?.toLongOrNull() ?: 0L,
                        movieTitle = movieTitle,
                        cinemaName = cinemaName,
                        eventStatus = status,
                        timestamp = System.currentTimeMillis(),
                        bookingUrl = bookingUrl,
                        triggeredVia = "FCM_PUSH"
                    )
                )
            }
        }
    }
}
