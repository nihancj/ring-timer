package com.example.ringtimer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.ringtimer.data.CallLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.content.Context
import com.example.ringtimer.data.ContactFilterOption
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

// ui/CallHistoryViewModel.kt
class CallHistoryViewModel(
    private val repository: CallLogRepository,
    private val appContext: Context
) : ViewModel() {

    private val _selectedContacts = MutableStateFlow<Set<String>>(emptySet())
    val selectedContacts: StateFlow<Set<String>> = _selectedContacts

    private val _contactOptions = MutableStateFlow<List<ContactFilterOption>>(emptyList())
    val contactOptions: StateFlow<List<ContactFilterOption>> = _contactOptions

    init {
        viewModelScope.launch {
            _contactOptions.value = repository.getDistinctContactOptions(appContext)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedEvents: Flow<PagingData<CallHistoryUiModel>> =
        _selectedContacts.flatMapLatest { selectedNames ->
            val numbers = _contactOptions.value
                .filter { it.displayName in selectedNames }
                .flatMap { it.numbers }
                .toSet()
            repository.getPagedEventsFlow(numbers)
        }.cachedIn(viewModelScope)

    fun toggleContact(name: String) {
        _selectedContacts.value =
            if (name in _selectedContacts.value) _selectedContacts.value - name
            else _selectedContacts.value + name
    }

    fun clearFilters() {
        _selectedContacts.value = emptySet()
    }
}

class CallHistoryViewModelFactory(
    private val repository: CallLogRepository,
    private val appContext: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CallHistoryViewModel(repository, appContext) as T
    }
}