package ru.sapozhnikov.aiagent.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Состояние задачи для диалога.
 *
 * @property conversationId идентификатор диалога (PK)
 * @property activeStage текущий активный этап
 * @property viewingStage этап, переписку которого просматривает пользователь
 */
@Entity(
    tableName = "task_states",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
internal data class TaskStateEntity(
    @PrimaryKey val conversationId: String,
    val activeStage: String,
    val viewingStage: String,
)
