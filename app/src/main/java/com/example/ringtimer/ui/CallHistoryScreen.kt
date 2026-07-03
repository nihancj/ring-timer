package com.example.ringtimer.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.ringtimer.R
import com.example.ringtimer.data.CallEvent
import com.example.ringtimer.data.CallLogRepository
import com.example.ringtimer.ui.theme.CallColors
import com.example.ringtimer.util.CallDateFormatter
import com.example.ringtimer.util.ContactLookupHelper

@Composable
fun CallHistoryScreen(repository: CallLogRepository) {
    val context = LocalContext.current
    val viewModel: CallHistoryViewModel = viewModel(
        factory = CallHistoryViewModelFactory(repository)
    )
    val events = viewModel.pagedEvents.collectAsLazyPagingItems()
    val isEmpty = events.loadState.refresh is LoadState.NotLoading && events.itemCount == 0
    val isLoading = events.loadState.refresh is LoadState.Loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CallColors.Background)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = CallColors.Answered
                )
            }

            isEmpty -> {
                EmptyCallHistory()
            }

            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        count = events.itemCount,
                        key = events.itemKey { model ->
                            when (model) {
                                is CallHistoryUiModel.DateHeader -> "header_${model.label}"
                                is CallHistoryUiModel.CallRow -> model.event.id
                            }
                        },
                        contentType = events.itemContentType { model ->
                            when (model) {
                                is CallHistoryUiModel.DateHeader -> "header"
                                is CallHistoryUiModel.CallRow -> "row"
                            }
                        }
                    ) { index ->
                        when (val model = events[index]) {
                            is CallHistoryUiModel.DateHeader -> DateHeaderRow(model.label)
                            is CallHistoryUiModel.CallRow -> CallHistoryRow(context, model.event)
                            null -> Unit
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateHeaderRow(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = CallColors.HeaderText,
        modifier = Modifier
            .fillMaxWidth()
            .background(CallColors.Background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun CallHistoryRow(context: Context, event: CallEvent) {
    val displayName = remember(event.phoneNumber) {
        ContactLookupHelper.getContactName(context, event.phoneNumber)
            ?: event.phoneNumber
            ?: "Unknown"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CallColors.Surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = if (event.wasAnswered) painterResource(id = R.drawable.call_received)
                    else painterResource(id = R.drawable.call_missed_incoming),
            contentDescription = if (event.wasAnswered) "Answered" else "Missed",
            tint = if (event.wasAnswered) CallColors.Answered else CallColors.Missed
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(displayName, color = CallColors.PrimaryText, style = MaterialTheme.typography.bodyLarge)
            Text(
                "${if (event.wasAnswered) "Answered" else "Missed"}  ~${event.estimatedRings} rings (${event.ringDurationMs / 1000}s)",
                color = CallColors.SecondaryText,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            CallDateFormatter.formatTimeOfDay(event.timestamp),
            color = CallColors.SecondaryText,
            style = MaterialTheme.typography.labelMedium
        )
    }

    HorizontalDivider(color = CallColors.Divider, thickness = 1.dp)
}

@Composable
fun EmptyCallHistory() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Call,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = CallColors.SecondaryText
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No calls yet",
            color = CallColors.PrimaryText,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Your call history will show up here once you receive or miss a call.",
            color = CallColors.SecondaryText,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}
