package ru.sapozhnikov.aiagent.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room-сущность сообщения в диалоге.
 *
 * @property id автоинкрементный идентификатор
 * @property conversationId идентификатор родительского диалога
 * @property text текст сообщения
 * @property role роль отправителя (USER / AI)
 * @property timestamp время создания (мс)
 * @property cacheHitTokens токены промпта из кэша
 * @property tokenCount число токенов сообщения
 * @property kind тип сообщения (TEXT / FILE)
 * @property attachmentUri URI сохранённого файла-вложения
 * @property attachmentFileName отображаемое имя прикреплённого файла
 * @property branchId идентификатор ветки (null — общая часть до checkpoint)
 */
@Entity(
    tableName = "messages",
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
internal data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String,
    val text: String,
    val role: String,
    val timestamp: Long,
    val cacheHitTokens: Int? = null,
    val tokenCount: Int? = null,
    val kind: String = "TEXT",
    val attachmentUri: String? = null,
    val attachmentFileName: String? = null,
    val branchId: String? = null,
)
