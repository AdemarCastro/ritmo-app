package com.ritmo.app.data.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

data class RemoteHabit(
    val id: String,
    val title: String,
    val description: String,
    val color: Int,
    val createdAt: Long,
    val archived: Boolean,
)

data class RemoteHabitCheckIn(
    val id: String,
    val habitId: String,
    val date: String,
    val completedAt: Long,
    val updatedAt: Long,
)

data class RemoteHabitSnapshot(
    val habits: List<RemoteHabit>,
    val checkIns: List<RemoteHabitCheckIn>,
)

interface HabitNetworkDataSource {
    suspend fun pushSnapshot(snapshot: RemoteHabitSnapshot)
    suspend fun pullSnapshot(): RemoteHabitSnapshot
}

@Singleton
class FakeHabitNetworkDataSource @Inject constructor() : HabitNetworkDataSource {
    private val mutex = Mutex()
    private var snapshot = RemoteHabitSnapshot(habits = emptyList(), checkIns = emptyList())

    override suspend fun pushSnapshot(snapshot: RemoteHabitSnapshot) {
        mutex.withLock {
            delay(SERVICE_LATENCY_MS)
            this.snapshot = snapshot
        }
    }

    override suspend fun pullSnapshot(): RemoteHabitSnapshot = mutex.withLock {
        delay(SERVICE_LATENCY_MS)
        snapshot
    }
}

private const val SERVICE_LATENCY_MS = 1_000L
