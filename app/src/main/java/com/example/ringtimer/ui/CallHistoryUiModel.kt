package com.example.ringtimer.ui

import com.example.ringtimer.data.CallEvent

sealed class CallHistoryUiModel {
    data class DateHeader(val label: String) : CallHistoryUiModel()
    data class CallRow(val event: CallEvent) : CallHistoryUiModel()
}