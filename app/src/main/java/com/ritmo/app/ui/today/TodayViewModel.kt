package com.ritmo.app.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritmo.app.data.HabitRepository
import com.ritmo.app.data.SyncStatus
import com.ritmo.app.data.TodayHabit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TodayUiState(
    val habits: List<TodayHabit> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val progress: Float = 0f,
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val isLoading: Boolean = true,
    val userMessage: String? = null,
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
) : ViewModel() {

    private val userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<TodayUiState> = combine(
        habitRepository.observeToday(),
        habitRepository.syncStatus,
        userMessage,
    ) { today, syncStatus, message ->
        TodayUiState(
            habits = today.habits,
            completedCount = today.completedCount,
            totalCount = today.totalCount,
            progress = today.progress,
            syncStatus = syncStatus,
            isLoading = false,
            userMessage = message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodayUiState(),
    )

    fun toggleHabit(habitId: String) {
        viewModelScope.launch {
            habitRepository.toggleTodayCheckIn(habitId)
        }
    }

    fun requestSync() {
        viewModelScope.launch {
            habitRepository.requestSync()
            userMessage.value = "Sincronização agendada"
        }
    }

    fun messageShown() {
        userMessage.value = null
    }
}
