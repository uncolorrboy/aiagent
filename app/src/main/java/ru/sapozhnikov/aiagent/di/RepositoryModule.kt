package ru.sapozhnikov.aiagent.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.sapozhnikov.aiagent.data.repository.AiAgentRepositoryImpl
import ru.sapozhnikov.aiagent.data.repository.ChatHistoryRepositoryImpl
import ru.sapozhnikov.aiagent.data.repository.ConversationBranchRepositoryImpl
import ru.sapozhnikov.aiagent.data.repository.ConversationFactsRepositoryImpl
import ru.sapozhnikov.aiagent.data.repository.ConversationMemoryRepositoryImpl
import ru.sapozhnikov.aiagent.data.repository.ConversationSummaryRepositoryImpl
import ru.sapozhnikov.aiagent.data.repository.ProfileMemoryRepositoryImpl
import ru.sapozhnikov.aiagent.data.repository.SettingsRepositoryImpl
import ru.sapozhnikov.aiagent.data.repository.WorkingMemoryRepositoryImpl
import ru.sapozhnikov.aiagent.domain.repository.AiAgentRepository
import ru.sapozhnikov.aiagent.domain.repository.ChatHistoryRepository
import ru.sapozhnikov.aiagent.domain.repository.ConversationBranchRepository
import ru.sapozhnikov.aiagent.domain.repository.ConversationFactsRepository
import ru.sapozhnikov.aiagent.domain.repository.ConversationMemoryRepository
import ru.sapozhnikov.aiagent.domain.repository.ConversationSummaryRepository
import ru.sapozhnikov.aiagent.domain.repository.ProfileMemoryRepository
import ru.sapozhnikov.aiagent.domain.repository.SettingsRepository
import ru.sapozhnikov.aiagent.domain.repository.WorkingMemoryRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAiAgentRepository(
        repository: AiAgentRepositoryImpl,
    ): AiAgentRepository

    @Binds
    @Singleton
    abstract fun bindChatHistoryRepository(
        repository: ChatHistoryRepositoryImpl,
    ): ChatHistoryRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        repository: SettingsRepositoryImpl,
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindConversationSummaryRepository(
        repository: ConversationSummaryRepositoryImpl,
    ): ConversationSummaryRepository

    @Binds
    @Singleton
    abstract fun bindConversationBranchRepository(
        repository: ConversationBranchRepositoryImpl,
    ): ConversationBranchRepository

    @Binds
    @Singleton
    abstract fun bindConversationFactsRepository(
        repository: ConversationFactsRepositoryImpl,
    ): ConversationFactsRepository

    @Binds
    @Singleton
    abstract fun bindWorkingMemoryRepository(
        repository: WorkingMemoryRepositoryImpl,
    ): WorkingMemoryRepository

    @Binds
    @Singleton
    abstract fun bindProfileMemoryRepository(
        repository: ProfileMemoryRepositoryImpl,
    ): ProfileMemoryRepository

    @Binds
    @Singleton
    abstract fun bindConversationMemoryRepository(
        repository: ConversationMemoryRepositoryImpl,
    ): ConversationMemoryRepository
}
