package com.ritmo.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ritmo.app.data.SyncState
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE archived = 0 ORDER BY createdAt ASC")
    fun observeActiveHabits(): Flow<List<LocalHabit>>

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    fun observeHabit(habitId: String): Flow<LocalHabit?>

    @Query("SELECT * FROM habit_check_ins WHERE date = :date")
    fun observeCheckInsForDate(date: String): Flow<List<LocalHabitCheckIn>>

    @Query("SELECT * FROM habit_check_ins WHERE habitId = :habitId ORDER BY date DESC")
    fun observeCheckInsForHabit(habitId: String): Flow<List<LocalHabitCheckIn>>

    @Query("SELECT * FROM habit_check_ins")
    fun observeAllCheckIns(): Flow<List<LocalHabitCheckIn>>

    @Query("SELECT COUNT(*) FROM habits WHERE syncState != 'SYNCED'")
    fun observePendingHabitCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM habit_check_ins WHERE syncState != 'SYNCED'")
    fun observePendingCheckInCount(): Flow<Int>

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    suspend fun getHabit(habitId: String): LocalHabit?

    @Query("SELECT * FROM habits")
    suspend fun getAllHabits(): List<LocalHabit>

    @Query("SELECT * FROM habit_check_ins")
    suspend fun getAllCheckIns(): List<LocalHabitCheckIn>

    @Query("SELECT * FROM habit_check_ins WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getCheckIn(habitId: String, date: String): LocalHabitCheckIn?

    @Upsert
    suspend fun upsertHabit(habit: LocalHabit)

    @Upsert
    suspend fun upsertCheckIn(checkIn: LocalHabitCheckIn)

    @Query(
        """
        UPDATE habits
        SET title = :title,
            description = :description,
            color = :color,
            syncState = :syncState
        WHERE id = :habitId
        """,
    )
    suspend fun updateHabit(
        habitId: String,
        title: String,
        description: String,
        color: Int,
        syncState: SyncState,
    )

    @Query("UPDATE habits SET archived = 1, syncState = :syncState WHERE id = :habitId")
    suspend fun archiveHabit(habitId: String, syncState: SyncState)

    @Query("DELETE FROM habit_check_ins WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCheckIn(habitId: String, date: String)

    @Query("UPDATE habits SET syncState = :syncState WHERE syncState != 'SYNCED'")
    suspend fun updateUnsyncedHabits(syncState: SyncState)

    @Query("UPDATE habit_check_ins SET syncState = :syncState WHERE syncState != 'SYNCED'")
    suspend fun updateUnsyncedCheckIns(syncState: SyncState)
}
