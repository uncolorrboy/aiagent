package ru.sapozhnikov.aiagent.presentation.chatlist

import ru.sapozhnikov.aiagent.domain.model.ConversationMode

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
 * @property mode режим диалога
 */
internal data class ConversationItem(
    val id: String,
    val title: String,
    val updatedAt: String,
    val mode: ConversationMode = ConversationMode.CHAT,
)
