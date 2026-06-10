package ru.sapozhnikov.aiagent.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.sapozhnikov.aiagent.data.local.dao.ConversationDao
import ru.sapozhnikov.aiagent.data.local.dao.MessageDao
import ru.sapozhnikov.aiagent.data.local.entity.ConversationEntity
import ru.sapozhnikov.aiagent.data.local.entity.MessageEntity

@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false,
)
internal abstract class AppDatabase : RoomDatabase() {

    abstract fun conversationDao(): ConversationDao

    abstract fun messageDao(): MessageDao
}
