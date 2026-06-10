package com.ritmo.app.data

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.Test

class HabitStatsCalculatorTest {

    private val today = LocalDate(2026, 6, 9)

    @Test
    fun calculate_withoutHabits_returnsEmptyStats() {
        val stats = HabitStatsCalculator.calculate(
            activeHabits = emptyList(),
            checkIns = emptyList(),
            today = today,
        )

        assertThat(stats.totalHabits).isEqualTo(0)
        assertThat(stats.completedToday).isEqualTo(0)
        assertThat(stats.weeklyCompletionRate).isEqualTo(0f)
        assertThat(stats.bestStreak).isEqualTo(0)
    }

    @Test
    fun calculate_withWeekCheckIns_returnsCompletionRateAndBestStreak() {
        val habits = listOf(
            habit(id = "read"),
            habit(id = "water"),
        )
        val checkIns = listOf(
            checkIn(habitId = "read", date = today),
            checkIn(habitId = "read", date = today.minus(DatePeriod(days = 1))),
            checkIn(habitId = "read", date = today.minus(DatePeriod(days = 2))),
            checkIn(habitId = "water", date = today),
        )

        val stats = HabitStatsCalculator.calculate(
            activeHabits = habits,
            checkIns = checkIns,
            today = today,
        )

        assertThat(stats.totalHabits).isEqualTo(2)
        assertThat(stats.completedToday).isEqualTo(2)
        assertThat(stats.weeklyCompletionRate).isEqualTo(4f / 14f)
        assertThat(stats.bestStreak).isEqualTo(3)
    }

    @Test
    fun streakForHabit_stopsAtFirstMissingDay() {
        val checkIns = listOf(
            checkIn(habitId = "read", date = today),
            checkIn(habitId = "read", date = today.minus(DatePeriod(days = 1))),
            checkIn(habitId = "read", date = today.minus(DatePeriod(days = 3))),
        )

        val streak = HabitStatsCalculator.streakForHabit(checkIns, today)

        assertThat(streak).isEqualTo(2)
    }

    private fun habit(id: String): Habit = Habit(
        id = id,
        title = id,
        description = "",
        color = 0xFF2E7D32.toInt(),
        createdAt = 0L,
        archived = false,
        syncState = SyncState.SYNCED,
    )

    private fun checkIn(habitId: String, date: LocalDate): HabitCheckIn = HabitCheckIn(
        id = "$habitId-$date",
        habitId = habitId,
        date = date,
        completedAt = 0L,
        syncState = SyncState.SYNCED,
        updatedAt = 0L,
    )
}
