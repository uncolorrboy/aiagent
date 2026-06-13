package ru.sapozhnikov.aiagent.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.domain.model.AiAgentMessage
import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage
import ru.sapozhnikov.aiagent.domain.model.Conversation
import ru.sapozhnikov.aiagent.domain.model.MessageRole
import ru.sapozhnikov.aiagent.domain.model.SavedUserFileMessage

/** Репозиторий для работы с историей чатов и сообщениями. */
internal interface ChatHistoryRepository {

    /** Наблюдает за списком диалогов, отсортированных по дате обновления. */
    fun observeConversations(): Flow<List<Conversation>>

    /** Наблюдает за сообщениями указанного диалога. */
    fun observeMessages(conversationId: String): Flow<List<ChatHistoryMessage>>

    /** Наблюдает за суммарным числом токенов в диалоге (последний запрос). */
    fun observeTotalTokenCount(conversationId: String): Flow<Int>

    /** Наблюдает за расчётной стоимостью диалога в USD. */
    fun observeChatCost(conversationId: String): Flow<Double>

    /** Возвращает все сообщения диалога для отображения в UI. */
    suspend fun getMessages(conversationId: String): List<ChatHistoryMessage>

    /**
     * Возвращает сообщения, подготовленные для отправки в LLM API.
     * Для файловых сообщений подставляется содержимое вложения.
     */
    suspend fun getMessagesForApi(conversationId: String): List<ChatHistoryMessage>

    /** Сохраняет текстовое сообщение в историю. */
    suspend fun saveMessage(conversationId: String, text: String, role: MessageRole)

    /**
     * Сохраняет пользовательское файловое сообщение: копирует файл локально
     * и создаёт запись в БД.
     *
     * @return содержимое файла и имя для отправки в API
     */
    suspend fun saveUserFileMessage(conversationId: String, sourceUri: Uri): SavedUserFileMessage

    /** Сохраняет ответ ассистента и обновляет статистику токенов последнего пользовательского сообщения. */
    suspend fun saveAiAgentMessage(conversationId: String, aiAgentMessage: AiAgentMessage)

    /** Создаёт запись диалога, если она ещё не существует. */
    suspend fun ensureConversationExists(conversationId: String)

    /** Обновляет заголовок диалога. */
    suspend fun updateConversationTitle(conversationId: String, title: String)

    /** Удаляет диалог вместе с сообщениями и файлами-вложениями. */
    suspend fun deleteConversation(conversationId: String)
}
