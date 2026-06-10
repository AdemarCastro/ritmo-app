package com.ritmo.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ritmo.app.data.SyncState

@Entity(tableName = "habits")
data class LocalHabit(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val color: Int,
    val createdAt: Long,
    val archived: Boolean,
    val syncState: SyncState,
)

@Entity(
    tableName = "habit_check_ins",
    foreignKeys = [
        ForeignKey(
            entity = LocalHabit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("habitId"),
        Index(value = ["habitId", "date"], unique = true),
    ],
)
data class LocalHabitCheckIn(
    @PrimaryKey val id: String,
    val habitId: String,
    val date: String,
    val completedAt: Long,
    val syncState: SyncState,
    val updatedAt: Long,
)
