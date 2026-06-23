package ru.sapozhnikov.aiagent.domain.interactor

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy
import ru.sapozhnikov.aiagent.domain.model.McpServerConnection
import ru.sapozhnikov.aiagent.domain.model.McpToolDefinition
import ru.sapozhnikov.aiagent.domain.repository.McpToolRepository
import ru.sapozhnikov.aiagent.domain.repository.SettingsRepository
import javax.inject.Inject

/** Use-case для управления настройками приложения. */
internal class SettingsInteractor @Inject constructor(
    private val repository: SettingsRepository,
    private val mcpToolRepository: McpToolRepository,
) {

    /** Наблюдает за выбранной стратегией управления контекстом. */
    fun observeContextManagementStrategy(): Flow<ContextManagementStrategy> {
        return repository.observeContextManagementStrategy()
    }

    /** Сохраняет выбранную стратегию управления контекстом. */
    suspend fun setContextManagementStrategy(strategy: ContextManagementStrategy) {
        repository.setContextManagementStrategy(strategy)
    }

    fun observeMcpEnabled(): Flow<Boolean> = repository.observeMcpEnabled()

    suspend fun setMcpEnabled(enabled: Boolean) {
        repository.setMcpEnabled(enabled)
        if (!enabled) {
            mcpToolRepository.disconnect()
            repository.clearMcpConnectionInfo()
        }
    }

    fun observeMcpServerUrl(): Flow<String> = repository.observeMcpServerUrl()

    suspend fun setMcpServerUrl(url: String) {
        repository.setMcpServerUrl(url)
    }

    fun observeMcpAuthToken(): Flow<String> = repository.observeMcpAuthToken()

    suspend fun setMcpAuthToken(token: String) {
        repository.setMcpAuthToken(token)
    }

    fun observeMcpServerName(): Flow<String?> = repository.observeMcpServerName()

    fun observeMcpServerVersion(): Flow<String?> = repository.observeMcpServerVersion()

    fun observeMcpTools(): Flow<List<McpToolDefinition>> = repository.observeMcpTools()

    suspend fun connectToMcpServer(
        serverUrl: String,
        authToken: String,
    ): Result<McpServerConnection> {
        return mcpToolRepository.connect(serverUrl, authToken)
            .onSuccess { connection ->
                repository.saveMcpConnectionInfo(
                    serverName = connection.serverName,
                    serverVersion = connection.serverVersion,
                    tools = connection.tools,
                )
            }
    }

    suspend fun disconnectFromMcpServer() {
        mcpToolRepository.disconnect()
        repository.clearMcpConnectionInfo()
    }
}
