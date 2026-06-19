package ru.sapozhnikov.aiagent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Room-сущность инварианта ассистента. */
@Entity(tableName = "assistant_invariants")
internal data class AssistantInvariantEntity(
    @PrimaryKey val id: String,
    val name: String,
    val text: String,
    val createdAt: Long,
)
