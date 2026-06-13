package ru.sapozhnikov.aiagent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room-сущность диалога (чата).
 *
 * @property id уникальный идентификатор
 * @property title заголовок диалога
 * @property createdAt время создания (мс)
 * @property updatedAt время последнего обновления (мс)
 */
@Entity(tableName = "conversations")
internal data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
)
