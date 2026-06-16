package ru.sapozhnikov.aiagent.presentation.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.sapozhnikov.aiagent.domain.interactor.MemoryInteractor
import ru.sapozhnikov.aiagent.domain.model.MemoryInstance
import ru.sapozhnikov.aiagent.navigation.Screen
import javax.inject.Inject

/**
 * ViewModel экрана создания и редактирования экземпляра памяти.
 */
@HiltViewModel
internal class MemoryInstanceEditorViewModel @Inject constructor(
    private val memoryInteractor: MemoryInteractor,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val memoryType: MemoryEditorType = checkNotNull(
        savedStateHandle.get<String>(Screen.MemoryEditor.MEMORY_TYPE_ARG),
    ).toMemoryEditorType()

    private val instanceId: String? = savedStateHandle
        .get<String>(Screen.MemoryEditor.INSTANCE_ID_ARG)
        ?.takeUnless { it == Screen.MemoryEditor.NEW_INSTANCE_ID }

    private val _uiState = MutableStateFlow(
        MemoryInstanceEditorUiState(
            memoryType = memoryType,
            isEditMode = instanceId != null,
            isLoading = instanceId != null,
        ),
    )
    val uiState = _uiState.asStateFlow()

    private val _saveCompleted = MutableStateFlow(false)
    val saveCompleted = _saveCompleted.asStateFlow()

    init {
        if (instanceId != null) {
            viewModelScope.launch {
                val instance = when (memoryType) {
                    MemoryEditorType.WORKING -> memoryInteractor.getWorkingMemoryForApi(instanceId)
                    MemoryEditorType.PROFILE -> memoryInteractor.getProfileMemoryForApi(instanceId)
                }
                _uiState.update { state ->
                    if (instance != null) {
                        state.copy(
                            name = instance.name,
                            text = instance.text,
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

            when (memoryType) {
                MemoryEditorType.WORKING -> {
                    if (instanceId != null) {
                        memoryInteractor.updateWorkingMemoryInstance(
                            MemoryInstance(
                                id = instanceId,
                                name = name,
                                text = text,
                            ),
                        )
                    } else {
                        memoryInteractor.createWorkingMemoryInstance(name, text)
                    }
                }
                MemoryEditorType.PROFILE -> {
                    if (instanceId != null) {
                        memoryInteractor.updateProfileMemoryInstance(
                            MemoryInstance(
                                id = instanceId,
                                name = name,
                                text = text,
                            ),
                        )
                    } else {
                        memoryInteractor.createProfileMemoryInstance(name, text)
                    }
                }
            }

            _uiState.update { it.copy(isSaving = false) }
            _saveCompleted.value = true
        }
    }

    private fun String.toMemoryEditorType(): MemoryEditorType {
        return MemoryEditorType.valueOf(this)
    }
}
