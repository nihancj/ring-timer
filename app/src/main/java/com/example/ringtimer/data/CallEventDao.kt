package com.example.ringtimer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.paging.PagingSource

@Dao
interface CallEventDao {
    @Insert
    suspend fun insert(event: CallEvent)

    @Query("SELECT * FROM call_events ORDER BY timestamp DESC")
    fun getAllPaged(): PagingSource<Int, CallEvent>
}