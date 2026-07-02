package com.example.ringtimer.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.ringtimer.data.CallLogRepository
import com.example.ringtimer.util.ContactLookupHelper

@Composable
fun CallHistoryScreen(repository: CallLogRepository) {
    val context = LocalContext.current
    val events = repository.getPagedEvents().collectAsLazyPagingItems()

    LazyColumn {
        items(
            count = events.itemCount,
            key = { index -> events.peek(index)?.id ?: index },
        ) { index ->
            events[index]?.let { event ->
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
}