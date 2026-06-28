package ru.sapozhnikov.aiagent.data.repository

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.data.local.SettingsDataStore
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy
import ru.sapozhnikov.aiagent.domain.model.McpServerConfig
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

    override fun observeMcpEnabled(): Flow<Boolean> = settingsDataStore.mcpEnabled

    override suspend fun setMcpEnabled(enabled: Boolean) {
        settingsDataStore.setMcpEnabled(enabled)
    }

    override suspend fun isMcpEnabled(): Boolean = settingsDataStore.isMcpEnabled()

    override fun observeMcpServers(): Flow<List<McpServerConfig>> = settingsDataStore.mcpServers

    override suspend fun getMcpServers(): List<McpServerConfig> = settingsDataStore.getMcpServers()

    override suspend fun saveMcpServers(servers: List<McpServerConfig>) {
        settingsDataStore.saveMcpServers(servers)
    }

    override suspend fun updateMcpServer(server: McpServerConfig) {
        settingsDataStore.updateMcpServer(server)
    }

    override suspend fun addMcpServer(server: McpServerConfig) {
        settingsDataStore.addMcpServer(server)
    }

    override suspend fun removeMcpServer(serverId: String) {
        settingsDataStore.removeMcpServer(serverId)
    }
}
