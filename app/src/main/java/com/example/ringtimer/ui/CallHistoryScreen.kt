package com.example.ringtimer.ui

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.ringtimer.R
import com.example.ringtimer.data.CallEvent
import com.example.ringtimer.data.CallLogRepository
import com.example.ringtimer.data.ContactFilterOption
import com.example.ringtimer.ui.theme.CallColors
import com.example.ringtimer.util.CallDateFormatter
import com.example.ringtimer.util.ContactLookupHelper

@Composable
fun CallHistoryScreen(repository: CallLogRepository) {
    val appContext = LocalContext.current.applicationContext
    val viewModel: CallHistoryViewModel = viewModel(
        factory = CallHistoryViewModelFactory(repository, appContext)
    )
    val events = viewModel.pagedEvents.collectAsLazyPagingItems()
    val contactOptions by viewModel.contactOptions.collectAsState()
    val selectedContacts by viewModel.selectedContacts.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    val closeSearch = {
        isSearchActive = false
        searchText = ""
    }

    BackHandler(enabled = isSearchActive) {
        closeSearch()
    }

    Column(modifier = Modifier.fillMaxSize().background(CallColors.Background)) {
        CallHistoryTopBar(
            isSearchActive = isSearchActive,
            searchText = searchText,
            onSearchTextChange = { searchText = it },
            selectedCount = selectedContacts.size,
            onSearchClick = { isSearchActive = true },
            onCloseSearch = closeSearch
        )

        if (isSearchActive) {
            ContactChipGrid(
                searchText = searchText,
                options = contactOptions,
                selected = selectedContacts,
                onToggle = viewModel::toggleContact,
                onClearAll = viewModel::clearFilters
            )
        } else {
            CallHistoryList(events = events, isFiltered = selectedContacts.isNotEmpty())
        }
    }
}

@Composable
fun CallHistoryTopBar(
    isSearchActive: Boolean,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    selectedCount: Int,
    onSearchClick: () -> Unit,
    onCloseSearch: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CallColors.TopBarBackground)
            .height(56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSearchActive) {
            IconButton(onClick = onCloseSearch) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = CallColors.TopBarPlaceholder)
            }

            TextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = { Text("Search contacts", color = CallColors.TopBarPlaceholder) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CallColors.TopBarBackground,
                    unfocusedContainerColor = CallColors.TopBarBackground,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = CallColors.TopBarText,
                    focusedTextColor = CallColors.TopBarText,
                    unfocusedTextColor = CallColors.TopBarText
                )
            )

            IconButton(onClick = onCloseSearch) {
                Icon(Icons.Default.Close, contentDescription = "Exit search", tint = CallColors.TopBarIcon)
            }

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        } else {
            Text(
                text = "Ring Timer",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = CallColors.TopBarText, // Or your custom CallColors.TopBarText
                textAlign = TextAlign.Start,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
            if (selectedCount > 0) {
                Badge(containerColor = CallColors.Answered) {
                    Text("$selectedCount", color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Search contacts", tint = CallColors.TopBarIcon)
            }
        }
    }
}

@Composable
fun ContactChipGrid(
    searchText: String,
    options: List<ContactFilterOption>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onClearAll: () -> Unit
) {
    val filteredOptions = remember(searchText, options) {
        if (searchText.isBlank()) options
        else options.filter { it.displayName.contains(searchText, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (selected.isNotEmpty()) {
            TextButton(onClick = onClearAll, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                Text("Clear all (${selected.size})", color = CallColors.Missed)
            }
        }

        if (filteredOptions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No matching contacts", color = CallColors.SecondaryText)
            }
        } else {
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredOptions.forEach { option ->
                    val isSelected = option.displayName in selected
                    FilterChip(
                        selected = isSelected,
                        onClick = { onToggle(option.displayName) },
                        label = { Text(option.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = CallColors.Surface,
                            selectedContainerColor = CallColors.Answered.copy(alpha = 0.15f),
                            labelColor = CallColors.SecondaryText,
                            selectedLabelColor = CallColors.Answered
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CallHistoryList(events: LazyPagingItems<CallHistoryUiModel>, isFiltered: Boolean) {
    val context = LocalContext.current
    val isEmpty = events.loadState.refresh is LoadState.NotLoading && events.itemCount == 0
    val isLoading = events.loadState.refresh is LoadState.Loading

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = CallColors.Answered
            )
            isEmpty -> EmptyCallHistory(isFiltered = isFiltered)
            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
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
fun ContactFilterRow(
    options: List<ContactFilterOption>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    if (options.isEmpty()) return

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(CallColors.Surface)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(options, key = { it.displayName }) { option ->
            val isSelected = option.displayName in selected
            FilterChip(
                selected = isSelected,
                onClick = { onToggle(option.displayName) },
                label = { Text(option.displayName) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CallColors.Surface,
                    selectedContainerColor = CallColors.Answered.copy(alpha = 0.15f),
                    labelColor = CallColors.SecondaryText,
                    selectedLabelColor = CallColors.Answered
                )
            )
        }
    }
}

@Composable
fun EmptyCallHistory(isFiltered: Boolean) {
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
            "Your call history will show up here once you receive or miss a call"
                    + if(isFiltered) " from this contact." else ".",
            color = CallColors.SecondaryText,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}
