package ru.sapozhnikov.aiagent.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Room-сущность блока фактов диалога (ключ-значение).
 *
 * @property conversationId идентификатор диалога (PK)
 * @property factsJson JSON-сериализованная карта фактов
 * @property updatedAt время последнего обновления (мс)
 */
@Entity(
    tableName = "conversation_facts",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
internal data class ConversationFactsEntity(
    @PrimaryKey val conversationId: String,
    val factsJson: String,
    val updatedAt: Long,
)
