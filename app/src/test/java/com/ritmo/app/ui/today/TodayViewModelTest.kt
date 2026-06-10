package com.ritmo.app.ui.today

import com.google.common.truth.Truth.assertThat
import com.ritmo.app.MainDispatcherRule
import com.ritmo.app.data.Habit
import com.ritmo.app.data.HabitDetail
import com.ritmo.app.data.HabitRepository
import com.ritmo.app.data.HabitStats
import com.ritmo.app.data.SyncState
import com.ritmo.app.data.SyncStatus
import com.ritmo.app.data.TodayHabit
import com.ritmo.app.data.TodaySummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class TodayViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiState_reflectsTodaySummaryAndSyncStatus() = runTest {
        val repository = FakeHabitRepository()
        val viewModel = TodayViewModel(repository)

        val state = viewModel.uiState.first { !it.isLoading }

        assertThat(state.totalCount).isEqualTo(2)
        assertThat(state.completedCount).isEqualTo(1)
        assertThat(state.progress).isEqualTo(0.5f)
        assertThat(state.syncStatus).isEqualTo(SyncStatus.PENDING)
    }

    @Test
    fun toggleHabit_delegatesToRepository() = runTest {
        val repository = FakeHabitRepository()
        val viewModel = TodayViewModel(repository)

        viewModel.toggleHabit("read")

        assertThat(repository.toggledHabitId).isEqualTo("read")
    }

    private class FakeHabitRepository : HabitRepository {
        var toggledHabitId: String? = null
        private val today = MutableStateFlow(
            TodaySummary(
                habits = listOf(
                    TodayHabit(habit("read"), checkedInToday = true),
                    TodayHabit(habit("water"), checkedInToday = false),
                ),
                completedCount = 1,
                totalCount = 2,
            ),
        )

        override val syncStatus: Flow<SyncStatus> = MutableStateFlow(SyncStatus.PENDING)

        override fun observeToday(): Flow<TodaySummary> = today

        override fun observeHabitDetail(habitId: String): Flow<HabitDetail?> {
            return MutableStateFlow(null)
        }

        override fun observeStats(): Flow<HabitStats> {
            return MutableStateFlow(HabitStats(0, 0, 0f, 0))
        }

        override suspend fun createHabit(title: String, description: String, color: Int): String = "id"

        override suspend fun updateHabit(
            habitId: String,
            title: String,
            description: String,
            color: Int,
        ) = Unit

        override suspend fun archiveHabit(habitId: String) = Unit

        override suspend fun toggleTodayCheckIn(habitId: String) {
            toggledHabitId = habitId
        }

        override suspend fun requestSync() = Unit

        override suspend fun syncPending() = Unit

        private fun habit(id: String): Habit = Habit(
            id = id,
            title = id,
            description = "",
            color = 0xFF2E7D32.toInt(),
            createdAt = 0L,
            archived = false,
            syncState = SyncState.SYNCED,
        )
    }
}
