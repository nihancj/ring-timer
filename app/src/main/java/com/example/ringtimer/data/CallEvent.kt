package com.example.ringtimer.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "call_events",
    indices = [Index(value = ["timestamp"])]
)
data class CallEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String?,
    val timestamp: Long,
    val ringDurationMs: Long,
    val estimatedRings: Int,
    val wasAnswered: Boolean
)