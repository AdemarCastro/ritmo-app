package com.ritmo.app.data

import com.ritmo.app.data.local.LocalHabit
import com.ritmo.app.data.local.LocalHabitCheckIn
import com.ritmo.app.data.network.RemoteHabit
import com.ritmo.app.data.network.RemoteHabitCheckIn
import kotlinx.datetime.LocalDate

fun LocalHabit.toExternal(): Habit = Habit(
    id = id,
    title = title,
    description = description,
    color = color,
    createdAt = createdAt,
    archived = archived,
    syncState = syncState,
)

fun Habit.toLocal(): LocalHabit = LocalHabit(
    id = id,
    title = title,
    description = description,
    color = color,
    createdAt = createdAt,
    archived = archived,
    syncState = syncState,
)

fun LocalHabitCheckIn.toExternal(): HabitCheckIn = HabitCheckIn(
    id = id,
    habitId = habitId,
    date = LocalDate.parse(date),
    completedAt = completedAt,
    syncState = syncState,
    updatedAt = updatedAt,
)

fun HabitCheckIn.toLocal(): LocalHabitCheckIn = LocalHabitCheckIn(
    id = id,
    habitId = habitId,
    date = date.toString(),
    completedAt = completedAt,
    syncState = syncState,
    updatedAt = updatedAt,
)

fun LocalHabit.toRemote(): RemoteHabit = RemoteHabit(
    id = id,
    title = title,
    description = description,
    color = color,
    createdAt = createdAt,
    archived = archived,
)

fun LocalHabitCheckIn.toRemote(): RemoteHabitCheckIn = RemoteHabitCheckIn(
    id = id,
    habitId = habitId,
    date = date,
    completedAt = completedAt,
    updatedAt = updatedAt,
)
