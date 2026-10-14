package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.db.AppSettingsEntity

@Composable
fun SettingsScreen(
    settings: AppSettingsEntity?,
    onSaveSettings: (AppSettingsEntity) -> Unit,
    onTriggerTestAlert: () -> Unit
) {
    val current = settings ?: AppSettingsEntity()

    var fdfsMorningShowOnly by remember(current) { mutableStateOf(current.fdfsMorningShowOnly) }
    var morningShowStartTime by remember(current) { mutableStateOf(current.morningShowStartTime) }
    var vibrationEnabled by remember(current) { mutableStateOf(current.vibrationEnabled) }
    var backendUrl by remember(current) { mutableStateOf(current.backendUrl) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "FDFS Alert Settings",
            style = MaterialTheme.typography.headlineMedium
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Alert & Morning Show Preferences",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("FDFS Morning Show Only", style = MaterialTheme.typography.bodyMedium)
                        Text("Alert specifically when morning shows open", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = fdfsMorningShowOnly,
                        onCheckedChange = {
                            fdfsMorningShowOnly = it
                            onSaveSettings(current.copy(fdfsMorningShowOnly = it))
                        }
                    )
                }

                OutlinedTextField(
                    value = morningShowStartTime,
                    onValueChange = {
                        morningShowStartTime = it
                        onSaveSettings(current.copy(morningShowStartTime = it))
                    },
                    label = { Text("Morning Show Start Time") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("30-Second Continuous Vibration", style = MaterialTheme.typography.bodyMedium)
                        Text("Vibrate continuously for 30s on FDFS alert", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = {
                            vibrationEnabled = it
                            onSaveSettings(current.copy(vibrationEnabled = it))
                        }
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Backend & Synchronization", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = backendUrl,
                    onValueChange = {
                        backendUrl = it
                        onSaveSettings(current.copy(backendUrl = it))
                    },
                    label = { Text("FastAPI Backend URL") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Testing & Diagnostics", style = MaterialTheme.typography.titleMedium)
                Button(
                    onClick = onTriggerTestAlert,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Trigger Test 30-Second Vibration Alert")
                }
            }
        }
    }
}
