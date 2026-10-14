package com.example.myapplication.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.db.*
import com.example.myapplication.engine.BookingStatusEngine
import com.example.myapplication.notification.NotificationHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val watchlistDao = db.watchlistDao()
    private val cinemaDao = db.cinemaDao()
    private val alertHistoryDao = db.alertHistoryDao()
    private val appSettingsDao = db.appSettingsDao()

    val watchlistState: StateFlow<List<WatchlistItemEntity>> = watchlistDao.getAllWatchlist()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val cinemasState: StateFlow<List<CinemaEntity>> = cinemaDao.getAllCinemas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val historyState: StateFlow<List<AlertHistoryEntity>> = alertHistoryDao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val settingsState: StateFlow<AppSettingsEntity?> = appSettingsDao.getSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        // Ensure default settings exist
        viewModelScope.launch {
            if (appSettingsDao.getSettings() == null) {
                appSettingsDao.saveSettings(AppSettingsEntity())
            }
        }
    }

    fun addWatchlistItem(movieTitle: String, cinemaName: String, targetDate: String) {
        viewModelScope.launch {
            watchlistDao.insertWatchlist(
                WatchlistItemEntity(
                    movieId = 0L,
                    movieTitle = movieTitle,
                    cinemaId = 0L,
                    cinemaName = cinemaName,
                    targetDate = targetDate,
                    isMorningShowOnly = true,
                    status = BookingStatus.UNOPENED,
                    isAlertEnabled = true
                )
            )
        }
    }

    fun deleteWatchlistItem(item: WatchlistItemEntity) {
        viewModelScope.launch {
            watchlistDao.deleteWatchlist(item)
        }
    }

    fun toggleWatchlistAlert(item: WatchlistItemEntity) {
        viewModelScope.launch {
            watchlistDao.updateWatchlist(item.copy(isAlertEnabled = !item.isAlertEnabled))
        }
    }

    fun checkWatchlistStatus(item: WatchlistItemEntity) {
        viewModelScope.launch {
            // Simulate status check: toggle between UNOPENED -> OPEN_SOON -> OPEN
            val nextStatus = when (item.status) {
                BookingStatus.UNOPENED -> BookingStatus.OPEN_SOON
                BookingStatus.OPEN_SOON -> BookingStatus.OPEN
                BookingStatus.OPEN -> BookingStatus.OPEN
                else -> BookingStatus.OPEN
            }

            val eval = BookingStatusEngine.evaluateTransition(
                previousStatus = item.status,
                fetchedStatus = nextStatus,
                bookingUrl = "https://www.google.com/search?q=${movieTitleQuery(item.movieTitle, item.cinemaName)}"
            )

            val updated = item.copy(
                status = eval.newStatus,
                bookingUrl = eval.bookingUrl,
                lastCheckedAt = System.currentTimeMillis()
            )
            watchlistDao.updateWatchlist(updated)

            if (eval.shouldTriggerAlert) {
                NotificationHelper.triggerBookingAlert(
                    context = getApplication(),
                    movieTitle = item.movieTitle,
                    cinemaName = item.cinemaName,
                    bookingUrl = eval.bookingUrl,
                    vibrationEnabled = true
                )

                alertHistoryDao.insertHistory(
                    AlertHistoryEntity(
                        watchlistItemId = item.id,
                        movieTitle = item.movieTitle,
                        cinemaName = item.cinemaName,
                        eventStatus = eval.newStatus,
                        timestamp = System.currentTimeMillis(),
                        bookingUrl = eval.bookingUrl,
                        triggeredVia = "MANUAL_CHECK"
                    )
                )
            }
        }
    }

    fun addCinema(name: String, city: String, location: String, format: String) {
        viewModelScope.launch {
            cinemaDao.insertCinema(
                CinemaEntity(
                    name = name,
                    city = city,
                    location = location,
                    format = format
                )
            )
        }
    }

    fun deleteCinema(cinema: CinemaEntity) {
        viewModelScope.launch {
            cinemaDao.deleteCinema(cinema)
        }
    }

    fun updateSettings(settings: AppSettingsEntity) {
        viewModelScope.launch {
            appSettingsDao.saveSettings(settings)
        }
    }

    fun clearAlertHistory() {
        viewModelScope.launch {
            alertHistoryDao.clearHistory()
        }
    }

    fun triggerTestAlert() {
        viewModelScope.launch {
            NotificationHelper.triggerBookingAlert(
                context = getApplication(),
                movieTitle = "Avatar 3: Fire & Ash (FDFS)",
                cinemaName = "PVR IMAX Forum Mall",
                bookingUrl = "https://www.google.com/search?q=Avatar+3+PVR+IMAX+booking",
                vibrationEnabled = true
            )
        }
    }

    private fun movieTitleQuery(title: String, cinema: String): String {
        return "${title.replace(" ", "+")}+${cinema.replace(" ", "+")}+booking"
    }
}
