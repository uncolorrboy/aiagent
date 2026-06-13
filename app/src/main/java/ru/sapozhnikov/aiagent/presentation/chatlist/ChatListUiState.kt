package ru.sapozhnikov.aiagent.presentation.chatlist

/**
 * Состояние UI экрана списка чатов.
 *
 * @property conversations список диалогов для отображения
 */
internal data class ChatListUiState(
    val conversations: List<ConversationItem> = emptyList(),
)

/**
 * UI-модель элемента списка диалогов.
 *
 * @property id идентификатор диалога
 * @property title заголовок диалога
 * @property updatedAt отформатированная дата последнего обновления
 */
internal data class ConversationItem(
    val id: String,
    val title: String,
    val updatedAt: String,
)
