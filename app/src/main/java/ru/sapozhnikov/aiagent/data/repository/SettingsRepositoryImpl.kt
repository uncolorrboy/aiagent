package ru.sapozhnikov.aiagent.data.repository

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.data.local.SettingsDataStore
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy
import ru.sapozhnikov.aiagent.domain.model.McpToolDefinition
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

    override fun observeMcpServerUrl(): Flow<String> = settingsDataStore.mcpServerUrl

    override suspend fun getMcpServerUrl(): String = settingsDataStore.getMcpServerUrl()

    override suspend fun getMcpAuthToken(): String = settingsDataStore.getMcpAuthToken()

    override suspend fun setMcpServerUrl(url: String) {
        settingsDataStore.setMcpServerUrl(url)
    }

    override fun observeMcpAuthToken(): Flow<String> = settingsDataStore.mcpAuthToken

    override suspend fun setMcpAuthToken(token: String) {
        settingsDataStore.setMcpAuthToken(token)
    }

    override fun observeMcpServerName(): Flow<String?> = settingsDataStore.mcpServerName

    override fun observeMcpServerVersion(): Flow<String?> = settingsDataStore.mcpServerVersion

    override fun observeMcpTools(): Flow<List<McpToolDefinition>> = settingsDataStore.mcpTools

    override suspend fun saveMcpConnectionInfo(
        serverName: String,
        serverVersion: String?,
        tools: List<McpToolDefinition>,
    ) {
        settingsDataStore.saveMcpConnectionInfo(serverName, serverVersion, tools)
    }

    override suspend fun clearMcpConnectionInfo() {
        settingsDataStore.clearMcpConnectionInfo()
    }
}
