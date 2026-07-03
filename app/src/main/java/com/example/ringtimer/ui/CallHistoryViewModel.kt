package com.example.ringtimer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.ringtimer.data.CallLogRepository
import kotlinx.coroutines.flow.Flow

class CallHistoryViewModel(repository: CallLogRepository) : ViewModel() {
    val pagedEvents: Flow<PagingData<CallHistoryUiModel>> =
        repository.getPagedEventsFlow().cachedIn(viewModelScope)
}

class CallHistoryViewModelFactory(
    private val repository: CallLogRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CallHistoryViewModel(repository) as T
    }
}