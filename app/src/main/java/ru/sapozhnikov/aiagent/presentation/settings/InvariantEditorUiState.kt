package ru.sapozhnikov.aiagent.presentation.settings

/**
 * Состояние UI экрана редактирования инварианта.
 */
internal data class InvariantEditorUiState(
    val isEditMode: Boolean = false,
    val name: String = "",
    val text: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
) {
    val canSave: Boolean
        get() = name.isNotBlank() && text.isNotBlank() && !isLoading && !isSaving

    val screenTitle: String
        get() = if (isEditMode) "Редактировать инвариант" else "Новый инвариант"
}
