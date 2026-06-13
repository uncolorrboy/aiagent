package ru.sapozhnikov.aiagent.data.local.mapper

import ru.sapozhnikov.aiagent.data.local.entity.ConversationSummaryEntity
import ru.sapozhnikov.aiagent.domain.model.ConversationSummary

internal fun ConversationSummaryEntity.toDomain(): ConversationSummary = ConversationSummary(
    conversationId = conversationId,
    text = text,
    coversUpToMessageId = coversUpToMessageId,
    updatedAt = updatedAt,
)

internal fun ConversationSummary.toEntity(): ConversationSummaryEntity = ConversationSummaryEntity(
    conversationId = conversationId,
    text = text,
    coversUpToMessageId = coversUpToMessageId,
    updatedAt = updatedAt,
)
