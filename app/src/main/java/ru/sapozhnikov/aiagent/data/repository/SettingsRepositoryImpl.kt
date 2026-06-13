package ru.sapozhnikov.aiagent.data.repository

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.data.local.SettingsDataStore
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy
import ru.sapozhnikov.aiagent.domain.repository.SettingsRepository
import javax.inject.Inject

/** Реализация [SettingsRepository] через [SettingsDataStore]. */
internal class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
) : SettingsRepository {

    override fun observeContextManagementStrategy(): Flow<ContextManagementStrategy> {
        return settingsDataStore.contextManagementStrategy
    }

    override suspend fun getContextManagementStrategy(): ContextManagementStrategy {
        return settingsDataStore.getContextManagementStrategy()
    }

    override suspend fun setContextManagementStrategy(strategy: ContextManagementStrategy) {
        settingsDataStore.setContextManagementStrategy(strategy)
    }
}
