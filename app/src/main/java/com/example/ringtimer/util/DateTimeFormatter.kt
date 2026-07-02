package com.example.ringtimer.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object CallDateFormatter {

    fun formatTimeOfDay(timestamp: Long): String {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }

    fun formatDateHeader(timestamp: Long): String {
        val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now()

        return when {
            date == today -> "Today"
            date == today.minusDays(1) -> "Yesterday"
            date.year == today.year -> date.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
            else -> date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
        }
    }

    fun dayKey(timestamp: Long): LocalDate =
        Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
}