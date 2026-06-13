package ru.sapozhnikov.aiagent.presentation.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.sapozhnikov.aiagent.domain.interactor.ChatHistoryInteractor
import ru.sapozhnikov.aiagent.domain.model.Conversation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel экрана списка чатов.
 *
 * @param chatHistoryInteractor use-case для работы с историей чатов
 */
@HiltViewModel
internal class ChatListViewModel @Inject constructor(
    private val chatHistoryInteractor: ChatHistoryInteractor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    /** Состояние UI экрана списка чатов. */
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            chatHistoryInteractor.observeConversations().collect { conversations ->
                _uiState.update {
                    it.copy(conversations = conversations.map { conversation -> conversation.toItem() })
                }
            }
        }
    }

    /**
     * Удаляет диалог по идентификатору.
     *
     * @param conversationId идентификатор удаляемого диалога
     */
    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            chatHistoryInteractor.deleteConversation(conversationId)
        }
    }

    private fun Conversation.toItem(): ConversationItem {
        val dateFormat = SimpleDateFormat("d MMM, HH:mm", Locale.forLanguageTag("ru"))
        return ConversationItem(
            id = id,
            title = title,
            updatedAt = dateFormat.format(Date(updatedAt)),
        )
    }
}
