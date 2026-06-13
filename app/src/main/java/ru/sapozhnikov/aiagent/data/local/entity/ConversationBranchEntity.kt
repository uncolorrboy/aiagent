package ru.sapozhnikov.aiagent.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room-сущность ветки диалога.
 *
 * @property id уникальный идентификатор ветки
 * @property conversationId идентификатор родительского диалога
 * @property name отображаемое имя ветки
 * @property checkpointMessageId id сообщения-checkpoint
 * @property isActive активна ли ветка в UI
 */
@Entity(
    tableName = "conversation_branches",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("conversationId")],
)
internal data class ConversationBranchEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val name: String,
    val checkpointMessageId: Long,
    val isActive: Boolean,
)
