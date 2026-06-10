package com.ritmo.app.data

import com.ritmo.app.data.local.HabitDao
import com.ritmo.app.data.local.LocalHabit
import com.ritmo.app.data.local.LocalHabitCheckIn
import com.ritmo.app.data.network.HabitNetworkDataSource
import com.ritmo.app.data.network.RemoteHabitSnapshot
import com.ritmo.app.data.sync.SyncScheduler
import com.ritmo.app.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface HabitRepository {
    val syncStatus: Flow<SyncStatus>

    fun observeToday(): Flow<TodaySummary>
    fun observeHabitDetail(habitId: String): Flow<HabitDetail?>
    fun observeStats(): Flow<HabitStats>

    suspend fun createHabit(title: String, description: String, color: Int): String
    suspend fun updateHabit(habitId: String, title: String, description: String, color: Int)
    suspend fun archiveHabit(habitId: String)
    suspend fun toggleTodayCheckIn(habitId: String)
    suspend fun requestSync()
    suspend fun syncPending()
}

@Singleton
class DefaultHabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val networkDataSource: HabitNetworkDataSource,
    private val syncScheduler: SyncScheduler,
    private val dateProvider: DateProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : HabitRepository {

    private val syncing = MutableStateFlow(false)
    private val syncError = MutableStateFlow(false)

    override val syncStatus: Flow<SyncStatus> = combine(
        habitDao.observePendingHabitCount(),
        habitDao.observePendingCheckInCount(),
        syncing,
        syncError,
    ) { pendingHabits, pendingCheckIns, isSyncing, hasError ->
        when {
            isSyncing -> SyncStatus.SYNCING
            hasError -> SyncStatus.ERROR
            pendingHabits + pendingCheckIns > 0 -> SyncStatus.PENDING
            else -> SyncStatus.IDLE
        }
    }

    override fun observeToday(): Flow<TodaySummary> {
        val today = dateProvider.today().toString()
        return combine(
            habitDao.observeActiveHabits(),
            habitDao.observeCheckInsForDate(today),
        ) { habits, checkIns ->
            val completedHabitIds = checkIns.map { it.habitId }.toSet()
            val items = habits.map { habit ->
                TodayHabit(
                    habit = habit.toExternal(),
                    checkedInToday = habit.id in completedHabitIds,
                )
            }
            TodaySummary(
                habits = items,
                completedCount = items.count { it.checkedInToday },
                totalCount = items.size,
            )
        }
    }

    override fun observeHabitDetail(habitId: String): Flow<HabitDetail?> {
        return combine(
            habitDao.observeHabit(habitId),
            habitDao.observeCheckInsForHabit(habitId),
        ) { habit, checkIns ->
            if (habit == null) {
                null
            } else {
                val externalCheckIns = checkIns.map { it.toExternal() }
                HabitDetail(
                    habit = habit.toExternal(),
                    checkIns = externalCheckIns,
                    currentStreak = HabitStatsCalculator.streakForHabit(
                        checkIns = externalCheckIns,
                        today = dateProvider.today(),
                    ),
                )
            }
        }
    }

    override fun observeStats(): Flow<HabitStats> {
        return combine(
            habitDao.observeActiveHabits(),
            habitDao.observeAllCheckIns(),
        ) { habits, checkIns ->
            HabitStatsCalculator.calculate(
                activeHabits = habits.map { it.toExternal() },
                checkIns = checkIns.map { it.toExternal() },
                today = dateProvider.today(),
            )
        }
    }

    override suspend fun createHabit(title: String, description: String, color: Int): String =
        withContext(ioDispatcher) {
            val id = UUID.randomUUID().toString()
            habitDao.upsertHabit(
                LocalHabit(
                    id = id,
                    title = title.trim(),
                    description = description.trim(),
                    color = color,
                    createdAt = dateProvider.nowMillis(),
                    archived = false,
                    syncState = SyncState.PENDING,
                ),
            )
            requestSync()
            id
        }

    override suspend fun updateHabit(
        habitId: String,
        title: String,
        description: String,
        color: Int,
    ) = withContext(ioDispatcher) {
        habitDao.updateHabit(
            habitId = habitId,
            title = title.trim(),
            description = description.trim(),
            color = color,
            syncState = SyncState.PENDING,
        )
        requestSync()
    }

    override suspend fun archiveHabit(habitId: String) = withContext(ioDispatcher) {
        habitDao.archiveHabit(habitId = habitId, syncState = SyncState.PENDING)
        requestSync()
    }

    override suspend fun toggleTodayCheckIn(habitId: String) = withContext(ioDispatcher) {
        val today = dateProvider.today().toString()
        val existing = habitDao.getCheckIn(habitId, today)
        if (existing == null) {
            val now = dateProvider.nowMillis()
            habitDao.upsertCheckIn(
                LocalHabitCheckIn(
                    id = "$habitId-$today",
                    habitId = habitId,
                    date = today,
                    completedAt = now,
                    updatedAt = now,
                    syncState = SyncState.PENDING,
                ),
            )
        } else {
            habitDao.deleteCheckIn(habitId = habitId, date = today)
        }
        requestSync()
    }

    override suspend fun requestSync() {
        syncError.value = false
        syncScheduler.schedule()
    }

    override suspend fun syncPending() = withContext(ioDispatcher) {
        syncing.value = true
        syncError.value = false
        try {
            habitDao.updateUnsyncedHabits(SyncState.SYNCING)
            habitDao.updateUnsyncedCheckIns(SyncState.SYNCING)

            val snapshot = RemoteHabitSnapshot(
                habits = habitDao.getAllHabits().map { it.toRemote() },
                checkIns = habitDao.getAllCheckIns().map { it.toRemote() },
            )
            networkDataSource.pushSnapshot(snapshot)

            habitDao.updateUnsyncedHabits(SyncState.SYNCED)
            habitDao.updateUnsyncedCheckIns(SyncState.SYNCED)
        } catch (exception: Exception) {
            habitDao.updateUnsyncedHabits(SyncState.ERROR)
            habitDao.updateUnsyncedCheckIns(SyncState.ERROR)
            syncError.value = true
            throw exception
        } finally {
            syncing.value = false
        }
    }
}
