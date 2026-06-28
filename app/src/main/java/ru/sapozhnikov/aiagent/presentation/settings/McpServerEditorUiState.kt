package ru.sapozhnikov.aiagent.presentation.settings

import ru.sapozhnikov.aiagent.domain.model.McpAuthType

/** Состояние UI экрана редактирования MCP-сервера. */
internal data class McpServerEditorUiState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val url: String = "",
    val authType: McpAuthType = McpAuthType.NONE,
    val authToken: String = "",
    val errorMessage: String? = null,
) {
    val screenTitle: String
        get() = if (isEditMode) "Редактировать сервер" else "Новый MCP-сервер"

    val canSave: Boolean
        get() = url.isNotBlank() && !isSaving

    val showBearerTokenField: Boolean
        get() = authType == McpAuthType.BEARER
}
