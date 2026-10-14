package com.example.myapplication.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies ORDER BY id DESC")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity): Long

    @Delete
    suspend fun deleteMovie(movie: MovieEntity): Int
}

@Dao
interface CinemaDao {
    @Query("SELECT * FROM cinemas ORDER BY id DESC")
    fun getAllCinemas(): Flow<List<CinemaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCinema(cinema: CinemaEntity): Long

    @Delete
    suspend fun deleteCinema(cinema: CinemaEntity): Int
}

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist ORDER BY id DESC")
    fun getAllWatchlist(): Flow<List<WatchlistItemEntity>>

    @Query("SELECT * FROM watchlist WHERE isAlertEnabled = 1")
    suspend fun getActiveWatchlistItems(): List<WatchlistItemEntity>

    @Query("SELECT * FROM watchlist WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WatchlistItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(item: WatchlistItemEntity): Long

    @Update
    suspend fun updateWatchlist(item: WatchlistItemEntity): Int

    @Delete
    suspend fun deleteWatchlist(item: WatchlistItemEntity): Int
}

@Dao
interface AlertHistoryDao {
    @Query("SELECT * FROM alert_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<AlertHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: AlertHistoryEntity): Long

    @Query("DELETE FROM alert_history")
    suspend fun clearHistory(): Int
}

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AppSettingsEntity): Long
}
