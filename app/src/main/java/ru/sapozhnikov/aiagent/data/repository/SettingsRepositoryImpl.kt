package ru.sapozhnikov.aiagent.data.repository

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.data.local.SettingsDataStore
import ru.sapozhnikov.aiagent.domain.repository.SettingsRepository
import javax.inject.Inject

/** Реализация [SettingsRepository] через [SettingsDataStore]. */
internal class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
) : SettingsRepository {

    override fun observeContextManagementEnabled(): Flow<Boolean> {
        return settingsDataStore.contextManagementEnabled
    }

    override suspend fun isContextManagementEnabled(): Boolean {
        return settingsDataStore.isContextManagementEnabled()
    }

    override suspend fun setContextManagementEnabled(enabled: Boolean) {
        settingsDataStore.setContextManagementEnabled(enabled)
    }
}
