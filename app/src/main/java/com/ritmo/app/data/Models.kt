package com.ritmo.app.data

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

enum class SyncState {
    SYNCED,
    PENDING,
    SYNCING,
    ERROR,
}

enum class SyncStatus {
    IDLE,
    PENDING,
    SYNCING,
    ERROR,
}

data class Habit(
    val id: String,
    val title: String,
    val description: String,
    val color: Int,
    val createdAt: Long,
    val archived: Boolean = false,
    val syncState: SyncState = SyncState.PENDING,
)

data class HabitCheckIn(
    val id: String,
    val habitId: String,
    val date: LocalDate,
    val completedAt: Long,
    val syncState: SyncState = SyncState.PENDING,
    val updatedAt: Long = completedAt,
)

data class TodayHabit(
    val habit: Habit,
    val checkedInToday: Boolean,
)

data class TodaySummary(
    val habits: List<TodayHabit>,
    val completedCount: Int,
    val totalCount: Int,
) {
    val progress: Float
        get() = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount
}

data class HabitDetail(
    val habit: Habit,
    val checkIns: List<HabitCheckIn>,
    val currentStreak: Int,
)

data class HabitStats(
    val totalHabits: Int,
    val completedToday: Int,
    val weeklyCompletionRate: Float,
    val bestStreak: Int,
)

object HabitStatsCalculator {

    fun calculate(
        activeHabits: List<Habit>,
        checkIns: List<HabitCheckIn>,
        today: LocalDate,
    ): HabitStats {
        if (activeHabits.isEmpty()) {
            return HabitStats(
                totalHabits = 0,
                completedToday = 0,
                weeklyCompletionRate = 0f,
                bestStreak = 0,
            )
        }

        val activeHabitIds = activeHabits.map { it.id }.toSet()
        val activeCheckIns = checkIns.filter { it.habitId in activeHabitIds }
        val weekDates = (0..6).map { today.minus(DatePeriod(days = it)) }.toSet()
        val completedToday = activeCheckIns.count { it.date == today }
        val completedThisWeek = activeCheckIns.count { it.date in weekDates }
        val expectedCompletions = activeHabits.size * weekDates.size
        val bestStreak = activeHabitIds.maxOfOrNull { habitId ->
            streakForHabit(
                checkIns = activeCheckIns.filter { it.habitId == habitId },
                today = today,
            )
        } ?: 0

        return HabitStats(
            totalHabits = activeHabits.size,
            completedToday = completedToday,
            weeklyCompletionRate = completedThisWeek.toFloat() / expectedCompletions,
            bestStreak = bestStreak,
        )
    }

    fun streakForHabit(checkIns: List<HabitCheckIn>, today: LocalDate): Int {
        val dates = checkIns.map { it.date }.toSet()
        var streak = 0
        var cursor = today

        while (cursor in dates) {
            streak += 1
            cursor = cursor.minus(DatePeriod(days = 1))
        }

        return streak
    }
}
