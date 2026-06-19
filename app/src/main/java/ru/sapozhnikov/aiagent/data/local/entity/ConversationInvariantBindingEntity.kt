package ru.sapozhnikov.aiagent.data.local.entity

import androidx.room.Entity

/** Room-сущность привязки инварианта к диалогу. */
@Entity(
    tableName = "conversation_invariant_bindings",
    primaryKeys = ["conversationId", "invariantId"],
)
internal data class ConversationInvariantBindingEntity(
    val conversationId: String,
    val invariantId: String,
)
