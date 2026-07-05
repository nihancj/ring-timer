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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
    val selectedTypes by viewModel.selectedTypes.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    val closeSearch = {
        isSearchActive = false
        searchText = ""
    }

    BackHandler(enabled = isSearchActive) {
        closeSearch()
    }

    Scaffold(
        topBar = {
            CallHistoryTopBar(
                isSearchActive = isSearchActive,
                searchText = searchText,
                onSearchTextChange = { searchText = it },
                selectedCount = selectedContacts.size,
                selectedTypes = selectedTypes,
                onSearchClick = { isSearchActive = true },
                onCloseSearch = closeSearch
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isSearchActive) {
                ContactChipGrid(
                    searchText = searchText,
                    options = contactOptions,
                    selected = selectedContacts,
                    selectedTypes = selectedTypes,
                    onToggle = viewModel::toggleContact,
                    onToggleType = viewModel::toggleType,
                    onClearAll = viewModel::clearFilters
                )
            } else {
                CallHistoryList(events = events, isFiltered = selectedContacts.isNotEmpty())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryTopBar(
    isSearchActive: Boolean,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    selectedCount: Int,
    selectedTypes: Set<CallTypeFilter>,
    onSearchClick: () -> Unit,
    onCloseSearch: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    TopAppBar(
        title = {
            if (isSearchActive) {
                TextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = { 
                        Text("Search contacts")
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
            } else {
                Text("Ring Timer", fontWeight = FontWeight.SemiBold)
            }
        },
        actions = {
            if (isSearchActive) {
                IconButton(onClick = onCloseSearch) {
                    Icon(Icons.Default.Close, contentDescription = "Exit search")
                }
            } else {
                if (selectedCount > 0 || selectedTypes.isNotEmpty()) {
                    var badgeText = ""
                    val badgeColor: Color
                    if (CallTypeFilter.ANSWERED in selectedTypes) {
                        badgeColor = Color(0xFF3E9C6F)
                        badgeText = "A"
                    } else if (CallTypeFilter.MISSED in selectedTypes) {
                        badgeColor = Color(0xFFE0554F)
                        badgeText = "M"
                    } else {
                        badgeColor = MaterialTheme.colorScheme.primary
                    }
                    if (selectedCount > 0) {
                        badgeText = selectedCount.toString()
                    }
                    Badge(
                        containerColor = badgeColor,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(badgeText, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Default.Search, contentDescription = "Search contacts")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun ContactChipGrid(
    searchText: String,
    options: List<ContactFilterOption>,
    selected: Set<String>,
    selectedTypes: Set<CallTypeFilter>,
    onToggle: (String) -> Unit,
    onToggleType: (CallTypeFilter) -> Unit,
    onClearAll: () -> Unit
) {
    val filteredOptions = remember(searchText, options) {
        if (searchText.isBlank()) options
        else options.filter { it.displayName.contains(searchText, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CallTypeChip(
                label = "Answered",
                isSelected = CallTypeFilter.ANSWERED in selectedTypes,
                badgeColor = Color(0xFF3E9C6F),
                onClick = { onToggleType(CallTypeFilter.ANSWERED) }
            )
            CallTypeChip(
                label = "Missed",
                isSelected = CallTypeFilter.MISSED in selectedTypes,
                badgeColor = Color(0xFFE0554F),
                onClick = { onToggleType(CallTypeFilter.MISSED) }
            )

            Spacer(modifier = Modifier.weight(1f))

            TextButton(
                onClick = onClearAll,
            ) {
                Text("Clear all (${selected.size})", color = MaterialTheme.colorScheme.error)
            }

        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

        if (filteredOptions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No matching contacts", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    )
                }
            }
        }
    }
}

@Composable
fun CallTypeChip(
    label: String,
    isSelected: Boolean,
    badgeColor: Color,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (isSelected) {
            {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color = badgeColor, shape = CircleShape)
                )
            }
        } else null
    )
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
                color = MaterialTheme.colorScheme.primary
            )
            isEmpty -> EmptyCallHistory(isFiltered = isFiltered)
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
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
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CallHistoryRow(context: Context, event: CallEvent) {
    val displayName = remember(event.phoneNumber) {
        ContactLookupHelper.getContactName(context, event.phoneNumber)
            ?: event.phoneNumber
            ?: "Unknown"
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = if (event.wasAnswered) painterResource(id = R.drawable.call_received)
                        else painterResource(id = R.drawable.call_missed_incoming),
                contentDescription = if (event.wasAnswered) "Answered" else "Missed",
                tint = if (event.wasAnswered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    displayName,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "${if (event.wasAnswered) "Answered" else "Missed"}  ~${event.estimatedRings} rings (${event.ringDurationMs / 1000}s)",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                CallDateFormatter.formatTimeOfDay(event.timestamp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun EmptyCallHistory(isFiltered: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Call,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No calls yet",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Your call history will show up here once you receive or miss a call"
                    + if(isFiltered) " from this contact." else ".",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}
