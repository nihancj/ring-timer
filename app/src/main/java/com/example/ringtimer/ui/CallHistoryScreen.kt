package com.example.ringtimer.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.ringtimer.data.CallLogRepository
import com.example.ringtimer.util.ContactLookupHelper

@Composable
fun CallHistoryScreen(repository: CallLogRepository) {
    val context = LocalContext.current
    val events by repository.getAllEvents().collectAsState(initial = emptyList())

    LazyColumn {
        items(events) { event ->
            val displayName = remember(event.phoneNumber) {
                ContactLookupHelper.getContactName(context, event.phoneNumber)
                    ?: event.phoneNumber
                    ?: "Unknown"
            }

            ListItem(
                headlineContent = { Text(displayName) },
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