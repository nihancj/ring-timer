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

    @Query("SELECT DISTINCT phoneNumber FROM call_events WHERE phoneNumber IS NOT NULL")
    suspend fun getDistinctPhoneNumbers(): List<String>

    @Query("""
    SELECT * FROM call_events 
    WHERE (:hasNumbers = 0 OR phoneNumber IN (:numbers))
    AND (:hasTypes = 0 OR wasAnswered IN (:answeredValues))
    ORDER BY timestamp DESC
""")
    fun getFilteredPaged(
        numbers: List<String>,
        hasNumbers: Boolean,
        answeredValues: List<Boolean>,
        hasTypes: Boolean
    ): PagingSource<Int, CallEvent>
}