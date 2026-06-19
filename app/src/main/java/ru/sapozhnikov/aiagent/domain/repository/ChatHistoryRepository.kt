package ru.sapozhnikov.aiagent.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.domain.model.AiAgentMessage
import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage
import ru.sapozhnikov.aiagent.domain.model.Conversation
import ru.sapozhnikov.aiagent.domain.model.ConversationMode
import ru.sapozhnikov.aiagent.domain.model.MessageRole
import ru.sapozhnikov.aiagent.domain.model.SavedUserFileMessage
import ru.sapozhnikov.aiagent.domain.model.TaskStage

/** Репозиторий для работы с историей чатов и сообщениями. */
internal interface ChatHistoryRepository {

    /** Наблюдает за списком диалогов, отсортированных по дате обновления. */
    fun observeConversations(): Flow<List<Conversation>>

    /** Наблюдает за сообщениями указанного диалога. */
    fun observeMessages(conversationId: String): Flow<List<ChatHistoryMessage>>

    /** Наблюдает за сообщениями конкретного этапа задачи. */
    fun observeMessagesForTaskStage(conversationId: String, taskStage: TaskStage): Flow<List<ChatHistoryMessage>>

    /** Наблюдает за сообщениями диалога с учётом активной ветки. */
    fun observeMessagesForBranch(conversationId: String, activeBranchId: String): Flow<List<ChatHistoryMessage>>

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

    /**
     * Возвращает сообщения для API с учётом активной ветки.
     */
    suspend fun getMessagesForApi(conversationId: String, activeBranchId: String): List<ChatHistoryMessage>

    /** Возвращает сообщения этапа задачи для API. */
    suspend fun getMessagesForApiByTaskStage(
        conversationId: String,
        taskStage: TaskStage,
    ): List<ChatHistoryMessage>

    /** Возвращает режим диалога. */
    suspend fun getConversationMode(conversationId: String): ConversationMode

    /** Сохраняет текстовое сообщение в историю. */
    suspend fun saveMessage(
        conversationId: String,
        text: String,
        role: MessageRole,
        branchId: String? = null,
        taskStage: TaskStage? = null,
    )

    /**
     * Сохраняет пользовательское файловое сообщение: копирует файл локально
     * и создаёт запись в БД.
     *
     * @return содержимое файла и имя для отправки в API
     */
    suspend fun saveUserFileMessage(
        conversationId: String,
        sourceUri: Uri,
        branchId: String? = null,
        taskStage: TaskStage? = null,
    ): SavedUserFileMessage

    /** Сохраняет ответ ассистента и обновляет статистику токенов последнего пользовательского сообщения. */
    suspend fun saveAiAgentMessage(
        conversationId: String,
        aiAgentMessage: AiAgentMessage,
        branchId: String? = null,
        taskStage: TaskStage? = null,
    )

    /** Создаёт запись диалога, если она ещё не существует. */
    suspend fun ensureConversationExists(
        conversationId: String,
        mode: ConversationMode = ConversationMode.CHAT,
    )

    /** Обновляет заголовок диалога. */
    suspend fun updateConversationTitle(conversationId: String, title: String)

    /** Удаляет диалог вместе с сообщениями и файлами-вложениями. */
    suspend fun deleteConversation(conversationId: String)
}
