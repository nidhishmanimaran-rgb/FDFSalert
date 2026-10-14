package com.example.myapplication.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BookingStatus {
    UNOPENED,
    OPEN_SOON,
    OPEN,
    UNKNOWN,
    CLOSED
}

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val language: String,
    val releaseDate: String,
    val posterUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cinemas")
data class CinemaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val city: String,
    val location: String,
    val format: String = "2D", // e.g., IMAX, 3D, 2D, 4DX
    val screenName: String = "Screen 1"
)

@Entity(tableName = "watchlist")
data class WatchlistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val movieId: Long,
    val movieTitle: String,
    val cinemaId: Long,
    val cinemaName: String,
    val targetDate: String, // YYYY-MM-DD
    val isMorningShowOnly: Boolean = true,
    val status: BookingStatus = BookingStatus.UNOPENED,
    val bookingUrl: String = "",
    val lastCheckedAt: Long = System.currentTimeMillis(),
    val isAlertEnabled: Boolean = true
)

@Entity(tableName = "alert_history")
data class AlertHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val watchlistItemId: Long,
    val movieTitle: String,
    val cinemaName: String,
    val eventStatus: BookingStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val bookingUrl: String = "",
    val triggeredVia: String = "LOCAL_MONITOR"
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val fdfsMorningShowOnly: Boolean = true,
    val morningShowStartTime: String = "06:00 AM",
    val vibrationEnabled: Boolean = true,
    val vibrationDurationSeconds: Int = 30,
    val alertSoundEnabled: Boolean = true,
    val syncIntervalMinutes: Int = 15,
    val backendUrl: String = "http://10.0.2.2:8000"
)
