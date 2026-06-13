package ru.sapozhnikov.aiagent.data.local.mapper

import ru.sapozhnikov.aiagent.data.local.entity.ConversationEntity
import ru.sapozhnikov.aiagent.data.local.entity.MessageEntity
import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage
import ru.sapozhnikov.aiagent.domain.model.Conversation
import ru.sapozhnikov.aiagent.domain.model.MessageKind
import ru.sapozhnikov.aiagent.domain.model.MessageRole

/** Преобразует [ConversationEntity] в доменную модель [Conversation]. */
internal fun ConversationEntity.toDomain(): Conversation = Conversation(
    id = id,
    title = title,
    updatedAt = updatedAt,
)

/** Преобразует [MessageEntity] в доменную модель [ChatHistoryMessage]. */
internal fun MessageEntity.toDomain(): ChatHistoryMessage = ChatHistoryMessage(
    id = id,
    conversationId = conversationId,
    text = text,
    role = role.toMessageRole(),
    timestamp = timestamp,
    cacheHitTokens = cacheHitTokens,
    tokenCount = tokenCount,
    kind = kind.toMessageKind(),
    attachmentUri = attachmentUri,
    attachmentFileName = attachmentFileName,
)

/** Преобразует [MessageRole] в строковое значение для Room. */
internal fun MessageRole.toEntityRole(): String = name

/** Преобразует [MessageKind] в строковое значение для Room. */
internal fun MessageKind.toEntityKind(): String = name

private fun String.toMessageRole(): MessageRole = MessageRole.valueOf(this)

private fun String.toMessageKind(): MessageKind = MessageKind.valueOf(this)
