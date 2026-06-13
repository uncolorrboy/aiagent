package ru.sapozhnikov.aiagent.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.sapozhnikov.aiagent.data.local.dao.ConversationBranchDao
import ru.sapozhnikov.aiagent.data.local.dao.ConversationDao
import ru.sapozhnikov.aiagent.data.local.dao.ConversationFactsDao
import ru.sapozhnikov.aiagent.data.local.dao.ConversationSummaryDao
import ru.sapozhnikov.aiagent.data.local.dao.MessageDao
import ru.sapozhnikov.aiagent.data.local.entity.ConversationBranchEntity
import ru.sapozhnikov.aiagent.data.local.entity.ConversationEntity
import ru.sapozhnikov.aiagent.data.local.entity.ConversationFactsEntity
import ru.sapozhnikov.aiagent.data.local.entity.ConversationSummaryEntity
import ru.sapozhnikov.aiagent.data.local.entity.MessageEntity

/** Локальная Room-база данных приложения. */
@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        ConversationSummaryEntity::class,
        ConversationBranchEntity::class,
        ConversationFactsEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
internal abstract class AppDatabase : RoomDatabase() {

    abstract fun conversationDao(): ConversationDao

    abstract fun messageDao(): MessageDao

    abstract fun conversationSummaryDao(): ConversationSummaryDao

    abstract fun conversationBranchDao(): ConversationBranchDao

    abstract fun conversationFactsDao(): ConversationFactsDao
}
