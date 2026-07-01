package com.example.ringtimer.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ringtimer.data.CallLogRepository

@Composable
fun CallHistoryScreen(repository: CallLogRepository) {
    val events by repository.getAllEvents().collectAsState(initial = emptyList())

    LazyColumn {
        items(events) { event ->
            ListItem(
                headlineContent = { Text(event.phoneNumber ?: "Unknown") },
                supportingContent = {
                    Text(
                        "${if (event.wasAnswered) "Answered" else "Missed"} · " +
                                "${event.ringDurationMs / 1000}s · ~${event.estimatedRings} rings"
                    )
                }
            )
        }
    }
}