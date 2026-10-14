package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.myapplication.fcm.FDFSFirebaseMessagingService
import com.example.myapplication.notification.NotificationHelper
import com.example.myapplication.ui.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Fetch FCM Token and register with backend
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FDFS_MainActivity", "Fetched FCM Token: $token")
                    FDFSFirebaseMessagingService.registerTokenWithBackend(this, token)
                } else {
                    Log.w("FDFS_MainActivity", "Fetching FCM registration token failed", task.exception)
                }
            }
        } catch (e: Exception) {
            Log.w("FDFS_MainActivity", "FirebaseMessaging not initialized yet: ${e.message}")
        }

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val watchlist by viewModel.watchlistState.collectAsState()
    val cinemas by viewModel.cinemasState.collectAsState()
    val history by viewModel.historyState.collectAsState()
    val settings by viewModel.settingsState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Movie, contentDescription = "Watchlist") },
                    label = { Text("Watchlist") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Theaters, contentDescription = "Cinemas") },
                    label = { Text("Cinemas") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> WatchlistScreen(
                    items = watchlist,
                    onAddItem = { title, cinema, date -> viewModel.addWatchlistItem(title, cinema, date) },
                    onDeleteItem = { viewModel.deleteWatchlistItem(it) },
                    onToggleAlert = { viewModel.toggleWatchlistAlert(it) },
                    onCheckStatus = { viewModel.checkWatchlistStatus(it) }
                )
                1 -> CinemasScreen(
                    cinemas = cinemas,
                    onAddCinema = { name, city, location, format -> viewModel.addCinema(name, city, location, format) },
                    onDeleteCinema = { viewModel.deleteCinema(it) }
                )
                2 -> AlertHistoryScreen(
                    historyItems = history,
                    onClearHistory = { viewModel.clearAlertHistory() }
                )
                3 -> SettingsScreen(
                    settings = settings,
                    onSaveSettings = { viewModel.updateSettings(it) },
                    onTriggerTestAlert = { viewModel.triggerTestAlert() }
                )
            }
        }
    }
}
