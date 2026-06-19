package ru.sapozhnikov.aiagent.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.sapozhnikov.aiagent.data.local.dao.AssistantInvariantDao
import ru.sapozhnikov.aiagent.data.local.dao.ConversationBranchDao
import ru.sapozhnikov.aiagent.data.local.dao.ConversationDao
import ru.sapozhnikov.aiagent.data.local.dao.ConversationFactsDao
import ru.sapozhnikov.aiagent.data.local.dao.ConversationInvariantBindingDao
import ru.sapozhnikov.aiagent.data.local.dao.ConversationMemoryBindingDao
import ru.sapozhnikov.aiagent.data.local.dao.ConversationSummaryDao
import ru.sapozhnikov.aiagent.data.local.dao.MessageDao
import ru.sapozhnikov.aiagent.data.local.dao.ProfileMemoryDao
import ru.sapozhnikov.aiagent.data.local.dao.TaskArtifactDao
import ru.sapozhnikov.aiagent.data.local.dao.TaskStateDao
import ru.sapozhnikov.aiagent.data.local.dao.WorkingMemoryDao
import ru.sapozhnikov.aiagent.data.local.entity.AssistantInvariantEntity
import ru.sapozhnikov.aiagent.data.local.entity.ConversationBranchEntity
import ru.sapozhnikov.aiagent.data.local.entity.ConversationEntity
import ru.sapozhnikov.aiagent.data.local.entity.ConversationFactsEntity
import ru.sapozhnikov.aiagent.data.local.entity.ConversationInvariantBindingEntity
import ru.sapozhnikov.aiagent.data.local.entity.ConversationMemoryBindingEntity
import ru.sapozhnikov.aiagent.data.local.entity.ConversationSummaryEntity
import ru.sapozhnikov.aiagent.data.local.entity.MessageEntity
import ru.sapozhnikov.aiagent.data.local.entity.ProfileMemoryEntity
import ru.sapozhnikov.aiagent.data.local.entity.TaskArtifactEntity
import ru.sapozhnikov.aiagent.data.local.entity.TaskStateEntity
import ru.sapozhnikov.aiagent.data.local.entity.WorkingMemoryEntity

/** Локальная Room-база данных приложения. */
@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        ConversationSummaryEntity::class,
        ConversationBranchEntity::class,
        ConversationFactsEntity::class,
        WorkingMemoryEntity::class,
        ProfileMemoryEntity::class,
        ConversationMemoryBindingEntity::class,
        TaskStateEntity::class,
        TaskArtifactEntity::class,
        AssistantInvariantEntity::class,
        ConversationInvariantBindingEntity::class,
    ],
    version = 9,
    exportSchema = false,
)
internal abstract class AppDatabase : RoomDatabase() {

    abstract fun conversationDao(): ConversationDao

    abstract fun messageDao(): MessageDao

    abstract fun conversationSummaryDao(): ConversationSummaryDao

    abstract fun conversationBranchDao(): ConversationBranchDao

    abstract fun conversationFactsDao(): ConversationFactsDao

    abstract fun workingMemoryDao(): WorkingMemoryDao

    abstract fun profileMemoryDao(): ProfileMemoryDao

    abstract fun conversationMemoryBindingDao(): ConversationMemoryBindingDao

    abstract fun taskStateDao(): TaskStateDao

    abstract fun taskArtifactDao(): TaskArtifactDao

    abstract fun assistantInvariantDao(): AssistantInvariantDao

    abstract fun conversationInvariantBindingDao(): ConversationInvariantBindingDao
}
