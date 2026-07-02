package com.example.ringtimer.data

import androidx.room.Database
import androidx.room.RoomDatabase

class AppDatabase {
    @Database(entities = [CallEvent::class], version = 1)
    abstract class AppDatabase : RoomDatabase() {
        abstract fun callEventDao(): CallEventDao
    }
}