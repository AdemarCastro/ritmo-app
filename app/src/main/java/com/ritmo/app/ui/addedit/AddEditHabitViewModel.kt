package com.ritmo.app.ui.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritmo.app.data.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditHabitUiState(
    val title: String = "",
    val description: String = "",
    val color: Int = HabitColors.first(),
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface AddEditHabitEvent {
    data object Saved : AddEditHabitEvent
}

@HiltViewModel
class AddEditHabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val habitId: String? = savedStateHandle["habitId"]
    private val _uiState = MutableStateFlow(AddEditHabitUiState(isEditMode = habitId != null))
    val uiState: StateFlow<AddEditHabitUiState> = _uiState

    private val eventsChannel = Channel<AddEditHabitEvent>(Channel.BUFFERED)
    val events = eventsChannel.receiveAsFlow()

    init {
        if (habitId != null) {
            viewModelScope.launch {
                habitRepository.observeHabitDetail(habitId).collect { detail ->
                    if (detail != null) {
                        _uiState.value = AddEditHabitUiState(
                            title = detail.habit.title,
                            description = detail.habit.description,
                            color = detail.habit.color,
                            isEditMode = true,
                        )
                    }
                }
            }
        }
    }

    fun updateTitle(value: String) {
        _uiState.update { it.copy(title = value, errorMessage = null) }
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun updateColor(value: Int) {
        _uiState.update { it.copy(color = value) }
    }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Informe um nome para o hábito") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            if (habitId == null) {
                habitRepository.createHabit(
                    title = state.title,
                    description = state.description,
                    color = state.color,
                )
            } else {
                habitRepository.updateHabit(
                    habitId = habitId,
                    title = state.title,
                    description = state.description,
                    color = state.color,
                )
            }
            eventsChannel.send(AddEditHabitEvent.Saved)
        }
    }
}

val HabitColors = listOf(
    0xFF2E7D32.toInt(),
    0xFF1565C0.toInt(),
    0xFF6A1B9A.toInt(),
    0xFFEF6C00.toInt(),
    0xFFC62828.toInt(),
)
