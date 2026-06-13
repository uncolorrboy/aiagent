package ru.sapozhnikov.aiagent.data.local.mapper

import ru.sapozhnikov.aiagent.data.local.entity.ConversationBranchEntity
import ru.sapozhnikov.aiagent.data.local.entity.ConversationFactsEntity
import ru.sapozhnikov.aiagent.domain.model.ConversationBranch
import ru.sapozhnikov.aiagent.domain.model.ConversationFacts

/** Преобразует [ConversationBranchEntity] в доменную модель. */
internal fun ConversationBranchEntity.toDomain(): ConversationBranch = ConversationBranch(
    id = id,
    conversationId = conversationId,
    name = name,
    checkpointMessageId = checkpointMessageId,
    isActive = isActive,
)

/** Преобразует [ConversationBranch] в Room-сущность. */
internal fun ConversationBranch.toEntity(): ConversationBranchEntity = ConversationBranchEntity(
    id = id,
    conversationId = conversationId,
    name = name,
    checkpointMessageId = checkpointMessageId,
    isActive = isActive,
)

/** Преобразует [ConversationFactsEntity] в доменную модель. */
internal fun ConversationFactsEntity.toDomain(facts: Map<String, String>): ConversationFacts = ConversationFacts(
    conversationId = conversationId,
    facts = facts,
    updatedAt = updatedAt,
)

/** Преобразует [ConversationFacts] в Room-сущность. */
internal fun ConversationFacts.toEntity(factsJson: String): ConversationFactsEntity = ConversationFactsEntity(
    conversationId = conversationId,
    factsJson = factsJson,
    updatedAt = updatedAt,
)
