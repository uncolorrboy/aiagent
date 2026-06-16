package ru.sapozhnikov.aiagent.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.sapozhnikov.aiagent.data.local.AppDatabase
import ru.sapozhnikov.aiagent.data.local.dao.ConversationBranchDao
import ru.sapozhnikov.aiagent.data.local.dao.ConversationDao
import ru.sapozhnikov.aiagent.data.local.dao.ConversationFactsDao
import ru.sapozhnikov.aiagent.data.local.dao.ConversationMemoryBindingDao
import ru.sapozhnikov.aiagent.data.local.dao.ConversationSummaryDao
import ru.sapozhnikov.aiagent.data.local.dao.MessageDao
import ru.sapozhnikov.aiagent.data.local.dao.ProfileMemoryDao
import ru.sapozhnikov.aiagent.data.local.dao.WorkingMemoryDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ai_agent.db",
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun provideConversationDao(database: AppDatabase): ConversationDao {
        return database.conversationDao()
    }

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    fun provideConversationSummaryDao(database: AppDatabase): ConversationSummaryDao {
        return database.conversationSummaryDao()
    }

    @Provides
    fun provideConversationBranchDao(database: AppDatabase): ConversationBranchDao {
        return database.conversationBranchDao()
    }

    @Provides
    fun provideConversationFactsDao(database: AppDatabase): ConversationFactsDao {
        return database.conversationFactsDao()
    }

    @Provides
    fun provideWorkingMemoryDao(database: AppDatabase): WorkingMemoryDao {
        return database.workingMemoryDao()
    }

    @Provides
    fun provideProfileMemoryDao(database: AppDatabase): ProfileMemoryDao {
        return database.profileMemoryDao()
    }

    @Provides
    fun provideConversationMemoryBindingDao(database: AppDatabase): ConversationMemoryBindingDao {
        return database.conversationMemoryBindingDao()
    }
}
