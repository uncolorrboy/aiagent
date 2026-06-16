package ru.sapozhnikov.aiagent.presentation.settings

/**
 * Состояние UI экрана редактирования экземпляра памяти.
 *
 * @property memoryType тип редактируемой памяти
 * @property isEditMode true при редактировании существующего экземпляра
 * @property name название экземпляра
 * @property text текстовое содержимое
 * @property isLoading true при загрузке данных для редактирования
 * @property isSaving true при сохранении
 */
internal data class MemoryInstanceEditorUiState(
    val memoryType: MemoryEditorType = MemoryEditorType.WORKING,
    val isEditMode: Boolean = false,
    val name: String = "",
    val text: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
) {
    val canSave: Boolean
        get() = name.isNotBlank() && text.isNotBlank() && !isLoading && !isSaving

    val screenTitle: String
        get() = when {
            memoryType == MemoryEditorType.WORKING && !isEditMode -> "Новая рабочая память"
            memoryType == MemoryEditorType.WORKING -> "Редактировать рабочую память"
            memoryType == MemoryEditorType.PROFILE && !isEditMode -> "Новый профиль"
            else -> "Редактировать профиль"
        }
}
