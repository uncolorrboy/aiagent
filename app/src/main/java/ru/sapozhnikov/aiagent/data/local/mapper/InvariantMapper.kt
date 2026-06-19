package ru.sapozhnikov.aiagent.data.local.mapper

import ru.sapozhnikov.aiagent.data.local.entity.AssistantInvariantEntity
import ru.sapozhnikov.aiagent.domain.model.AssistantInvariant

internal fun AssistantInvariantEntity.toDomain(): AssistantInvariant = AssistantInvariant(
    id = id,
    name = name,
    text = text,
)

internal fun AssistantInvariant.toEntity(createdAt: Long): AssistantInvariantEntity = AssistantInvariantEntity(
    id = id,
    name = name,
    text = text,
    createdAt = createdAt,
)
