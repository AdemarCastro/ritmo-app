package com.ritmo.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [LocalHabit::class, LocalHabitCheckIn::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(RitmoConverters::class)
abstract class RitmoDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}
