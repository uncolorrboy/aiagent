package ru.sapozhnikov.aiagent.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.sapozhnikov.aiagent.data.repository.AiAgentRepositoryImpl
import ru.sapozhnikov.aiagent.domain.repository.AiAgentRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAiAgentRepository(
        repository: AiAgentRepositoryImpl,
    ): AiAgentRepository
}
