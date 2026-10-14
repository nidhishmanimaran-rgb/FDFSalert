package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.db.BookingStatus
import com.example.myapplication.data.db.WatchlistItemEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    items: List<WatchlistItemEntity>,
    onAddItem: (movieTitle: String, cinemaName: String, targetDate: String) -> Unit,
    onDeleteItem: (WatchlistItemEntity) -> Unit,
    onToggleAlert: (WatchlistItemEntity) -> Unit,
    onCheckStatus: (WatchlistItemEntity) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var movieTitleInput by remember { mutableStateOf("") }
    var cinemaNameInput by remember { mutableStateOf("") }
    var targetDateInput by remember { mutableStateOf("2026-11-15") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add FDFS Watchlist")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "FDFS Alert Watchlist",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No movies in watchlist.\nTap '+' to add an FDFS alert!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        WatchlistItemCard(
                            item = item,
                            onDelete = { onDeleteItem(item) },
                            onToggle = { onToggleAlert(item) },
                            onCheck = { onCheckStatus(item) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add FDFS Movie Watchlist") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = movieTitleInput,
                        onValueChange = { movieTitleInput = it },
                        label = { Text("Movie Title (e.g. Avatar 3)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cinemaNameInput,
                        onValueChange = { cinemaNameInput = it },
                        label = { Text("Cinema / Theater (e.g. PVR IMAX)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = targetDateInput,
                        onValueChange = { targetDateInput = it },
                        label = { Text("Release / FDFS Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (movieTitleInput.isNotBlank() && cinemaNameInput.isNotBlank()) {
                            onAddItem(movieTitleInput, cinemaNameInput, targetDateInput)
                            movieTitleInput = ""
                            cinemaNameInput = ""
                            showDialog = false
                        }
                    }
                ) {
                    Text("Add Alert")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun WatchlistItemCard(
    item: WatchlistItemEntity,
    onDelete: () -> Unit,
    onToggle: () -> Unit,
    onCheck: () -> Unit
) {
    val statusColor = when (item.status) {
        BookingStatus.OPEN -> Color(0xFF2E7D32)
        BookingStatus.OPEN_SOON -> Color(0xFFEF6C00)
        BookingStatus.UNOPENED -> Color(0xFF1565C0)
        BookingStatus.CLOSED -> Color(0xFFC62828)
        BookingStatus.UNKNOWN -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.movieTitle,
                    style = MaterialTheme.typography.titleLarge
                )
                AssistChip(
                    onClick = { },
                    label = { Text(item.status.name) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = statusColor.copy(alpha = 0.15f),
                        labelColor = statusColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Cinema: ${item.cinemaName}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Target Date: ${item.targetDate} (Morning Show Only)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = item.isAlertEnabled,
                        onCheckedChange = { onToggle() }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (item.isAlertEnabled) "Alert Active" else "Paused")
                }

                Row {
                    IconButton(onClick = onCheck) {
                        Icon(Icons.Default.Refresh, contentDescription = "Check Status")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
