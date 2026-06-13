package ru.sapozhnikov.aiagent.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room-сущность резюме диалога для сжатия контекста LLM.
 *
 * @property conversationId идентификатор диалога (первичный ключ)
 * @property text текст резюме
 * @property coversUpToMessageId id последнего сообщения, включённого в резюме
 * @property updatedAt время последнего обновления (мс)
 */
@Entity(
    tableName = "conversation_summaries",
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
internal data class ConversationSummaryEntity(
    @PrimaryKey val conversationId: String,
    val text: String,
    val coversUpToMessageId: Long,
    val updatedAt: Long,
)
