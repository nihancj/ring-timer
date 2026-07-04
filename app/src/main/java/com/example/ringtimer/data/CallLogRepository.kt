package com.example.ringtimer.data

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.PagingConfig
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.room.Room
import com.example.ringtimer.reciever.CallStateReceiver.Companion.estimateRingCount
import com.example.ringtimer.ui.CallHistoryUiModel
import com.example.ringtimer.util.CallDateFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CallLogRepository private constructor(context: Context) {
    private val dao = Room.databaseBuilder(
        context.applicationContext, AppDatabase.AppDatabase::class.java, "calls.db"
    ).build().callEventDao()

    fun logCall(number: String?, durationMs: Long, answered: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.insert(
                CallEvent(
                    phoneNumber = number,
                    timestamp = System.currentTimeMillis() - durationMs,
                    ringDurationMs = durationMs,
                    estimatedRings = estimateRingCount(durationMs),
                    wasAnswered = answered
                )
            )
        }
    }

    fun getPagedEventsFlow(): Flow<PagingData<CallHistoryUiModel>> =
        Pager(PagingConfig(pageSize = 50)) { dao.getAllPaged() }.flow
            .map { pagingData ->
                pagingData
                    .map { CallHistoryUiModel.CallRow(it) as CallHistoryUiModel }
                    .insertSeparators { before, after ->
                        val afterRow = after as? CallHistoryUiModel.CallRow ?: return@insertSeparators null
                        val beforeRow = before as? CallHistoryUiModel.CallRow
                        val afterDay = CallDateFormatter.dayKey(afterRow.event.timestamp)
                        val beforeDay = beforeRow?.let { CallDateFormatter.dayKey(it.event.timestamp) }
                        if (beforeDay != afterDay) {
                            CallHistoryUiModel.DateHeader(CallDateFormatter.formatDateHeader(afterRow.event.timestamp))
                        } else null
                    }
            }

    companion object {
        @Volatile private var instance: CallLogRepository? = null
        fun getInstance(context: Context): CallLogRepository =
            instance ?: synchronized(this) {
                instance ?: CallLogRepository(context).also { instance = it }
            }
    }
}