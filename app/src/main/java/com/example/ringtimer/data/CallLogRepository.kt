package com.example.ringtimer.data

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.PagingConfig
import androidx.room.Room
import com.example.ringtimer.reciever.CallStateReceiver.Companion.estimateRingCount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
                    timestamp = System.currentTimeMillis(),
                    ringDurationMs = durationMs,
                    estimatedRings = estimateRingCount(durationMs),
                    wasAnswered = answered
                )
            )
        }
    }

    fun getPagedEvents(): Flow<PagingData<CallEvent>> =
        Pager(PagingConfig(pageSize = 50)) { dao.getAllPaged() }.flow

    companion object {
        @Volatile private var instance: CallLogRepository? = null
        fun getInstance(context: Context): CallLogRepository =
            instance ?: synchronized(this) {
                instance ?: CallLogRepository(context).also { instance = it }
            }
    }
}