package ru.sapozhnikov.aiagent.data.local.mapper

import ru.sapozhnikov.aiagent.data.local.entity.ConversationEntity
import ru.sapozhnikov.aiagent.data.local.entity.MessageEntity
import ru.sapozhnikov.aiagent.domain.model.ChatHistoryMessage
import ru.sapozhnikov.aiagent.domain.model.Conversation
import ru.sapozhnikov.aiagent.domain.model.MessageKind
import ru.sapozhnikov.aiagent.domain.model.MessageRole

internal fun ConversationEntity.toDomain(): Conversation = Conversation(
    id = id,
    title = title,
    updatedAt = updatedAt,
)

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

internal fun MessageRole.toEntityRole(): String = name

internal fun MessageKind.toEntityKind(): String = name

private fun String.toMessageRole(): MessageRole = MessageRole.valueOf(this)

private fun String.toMessageKind(): MessageKind = MessageKind.valueOf(this)
