package ru.sapozhnikov.aiagent.data.local.mapper

import ru.sapozhnikov.aiagent.data.local.entity.ConversationMemoryBindingEntity
import ru.sapozhnikov.aiagent.data.local.entity.ProfileMemoryEntity
import ru.sapozhnikov.aiagent.data.local.entity.WorkingMemoryEntity
import ru.sapozhnikov.aiagent.domain.model.ConversationMemorySelection
import ru.sapozhnikov.aiagent.domain.model.MemoryInstance

internal fun WorkingMemoryEntity.toDomain(): MemoryInstance = MemoryInstance(
    id = id,
    name = name,
    text = text,
)

internal fun ProfileMemoryEntity.toDomain(): MemoryInstance = MemoryInstance(
    id = id,
    name = name,
    text = text,
)

internal fun ConversationMemoryBindingEntity.toDomain(): ConversationMemorySelection =
    ConversationMemorySelection(
        conversationId = conversationId,
        workingMemoryId = workingMemoryId,
        profileMemoryId = profileMemoryId,
    )

internal fun ConversationMemorySelection.toEntity(): ConversationMemoryBindingEntity =
    ConversationMemoryBindingEntity(
        conversationId = conversationId,
        workingMemoryId = workingMemoryId,
        profileMemoryId = profileMemoryId,
    )
