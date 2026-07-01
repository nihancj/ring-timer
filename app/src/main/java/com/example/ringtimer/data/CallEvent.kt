package com.example.ringtimer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

class CallEvent {
    @Entity(tableName = "call_events")
    data class CallEvent(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        val phoneNumber: String?,
        val timestamp: Long,
        val ringDurationMs: Long,
        val estimatedRings: Int,
        val wasAnswered: Boolean
    )
}