package ru.sapozhnikov.aiagent.data.local.mapper

import ru.sapozhnikov.aiagent.data.local.entity.ConversationSummaryEntity
import ru.sapozhnikov.aiagent.domain.model.ConversationSummary

/** Преобразует [ConversationSummaryEntity] в доменную модель. */
internal fun ConversationSummaryEntity.toDomain(): ConversationSummary = ConversationSummary(
    conversationId = conversationId,
    text = text,
    coversUpToMessageId = coversUpToMessageId,
    updatedAt = updatedAt,
)

/** Преобразует доменную модель [ConversationSummary] в Room-сущность. */
internal fun ConversationSummary.toEntity(): ConversationSummaryEntity = ConversationSummaryEntity(
    conversationId = conversationId,
    text = text,
    coversUpToMessageId = coversUpToMessageId,
    updatedAt = updatedAt,
)
