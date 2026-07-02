package com.example.ringtimer.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import com.example.ringtimer.util.BatteryOptimizationHelper
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun BatteryOptimizationPrompt(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember {
        mutableStateOf(!BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context))
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                onDismiss()
            },
            title = { Text("Keep ring tracking reliable") },
            text = {
                Text(
                    "Some phone manufacturers aggressively close background apps to save " +
                            "battery, which can cause missed calls to go untracked. Disabling battery " +
                            "optimization for this app keeps call detection running reliably."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                    showDialog = false
                    onDismiss()
                }) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    onDismiss()
                }) {
                    Text("Not now")
                }
            }
        )
    }
}