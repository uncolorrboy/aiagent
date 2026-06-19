package ru.sapozhnikov.aiagent.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Артефакт завершённого этапа задачи.
 *
 * @property id автоинкрементный идентификатор
 * @property conversationId идентификатор диалога
 * @property stage этап, к которому относится артефакт
 * @property content текстовое содержимое
 * @property createdAt время создания (мс)
 */
@Entity(
    tableName = "task_artifacts",
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
internal data class TaskArtifactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String,
    val stage: String,
    val content: String,
    val createdAt: Long,
)
