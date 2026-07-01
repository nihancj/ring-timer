package com.example.ringtimer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

class CallEventDao {
    @Dao
    interface CallEventDao {
        @Insert
        suspend fun insert(event: CallEvent.CallEvent)

        @Query("SELECT * FROM call_events ORDER BY timestamp DESC")
        fun getAll(): Flow<List<CallEvent.CallEvent>>
    }
}