package com.ritmo.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritmo.app.data.HabitRepository
import com.ritmo.app.data.HabitStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    habitRepository: HabitRepository,
) : ViewModel() {
    val stats: StateFlow<HabitStats> = habitRepository.observeStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitStats(
                totalHabits = 0,
                completedToday = 0,
                weeklyCompletionRate = 0f,
                bestStreak = 0,
            ),
        )
}
