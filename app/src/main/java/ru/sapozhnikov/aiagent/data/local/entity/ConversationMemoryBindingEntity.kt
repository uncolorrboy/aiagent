package ru.sapozhnikov.aiagent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room-сущность привязки памяти к диалогу.
 *
 * Выбор рабочей и долговременной памяти делается один раз на диалог.
 */
@Entity(tableName = "conversation_memory_bindings")
internal data class ConversationMemoryBindingEntity(
    @PrimaryKey val conversationId: String,
    val workingMemoryId: String?,
    val profileMemoryId: String?,
)
