package ru.sapozhnikov.aiagent.presentation.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.sapozhnikov.aiagent.domain.interactor.InvariantInteractor
import ru.sapozhnikov.aiagent.domain.model.AssistantInvariant
import ru.sapozhnikov.aiagent.navigation.Screen
import javax.inject.Inject

/** ViewModel экрана создания и редактирования инварианта. */
@HiltViewModel
internal class InvariantEditorViewModel @Inject constructor(
    private val invariantInteractor: InvariantInteractor,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val invariantId: String? = savedStateHandle
        .get<String>(Screen.InvariantEditor.INVARIANT_ID_ARG)
        ?.takeUnless { it == Screen.InvariantEditor.NEW_INVARIANT_ID }

    private val _uiState = MutableStateFlow(
        InvariantEditorUiState(
            isEditMode = invariantId != null,
            isLoading = invariantId != null,
        ),
    )
    val uiState = _uiState.asStateFlow()

    private val _saveCompleted = MutableStateFlow(false)
    val saveCompleted = _saveCompleted.asStateFlow()

    init {
        if (invariantId != null) {
            viewModelScope.launch {
                val invariant = invariantInteractor.getInvariantById(invariantId)
                _uiState.update { state ->
                    if (invariant != null) {
                        state.copy(
                            name = invariant.name,
                            text = invariant.text,
                            isLoading = false,
                        )
                    } else {
                        state.copy(isLoading = false)
                    }
                }
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onTextChanged(text: String) {
        _uiState.update { it.copy(text = text) }
    }

    fun onSaveClicked() {
        val state = _uiState.value
        if (!state.canSave) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val name = state.name.trim()
            val text = state.text.trim()

            if (invariantId != null) {
                invariantInteractor.updateInvariant(
                    AssistantInvariant(
                        id = invariantId,
                        name = name,
                        text = text,
                    ),
                )
            } else {
                invariantInteractor.createInvariant(
                    name = name,
                    text = text,
                )
            }

            _uiState.update { it.copy(isSaving = false) }
            _saveCompleted.value = true
        }
    }
}
