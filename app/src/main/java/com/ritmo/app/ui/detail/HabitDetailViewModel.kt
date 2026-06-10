package com.ritmo.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritmo.app.data.HabitDetail
import com.ritmo.app.data.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitDetailUiState(
    val detail: HabitDetail? = null,
    val isLoading: Boolean = true,
)

sealed interface HabitDetailEvent {
    data object Archived : HabitDetailEvent
}

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val habitId: String = checkNotNull(savedStateHandle["habitId"])

    val uiState: StateFlow<HabitDetailUiState> = habitRepository.observeHabitDetail(habitId)
        .map { detail -> HabitDetailUiState(detail = detail, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitDetailUiState(),
        )

    private val eventsChannel = Channel<HabitDetailEvent>(Channel.BUFFERED)
    val events = eventsChannel.receiveAsFlow()

    fun archiveHabit() {
        viewModelScope.launch {
            habitRepository.archiveHabit(habitId)
            eventsChannel.send(HabitDetailEvent.Archived)
        }
    }
}
