package ru.sapozhnikov.aiagent.domain.interactor

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.app.McpOAuthSessionManager
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy
import ru.sapozhnikov.aiagent.domain.model.McpAuthType
import ru.sapozhnikov.aiagent.domain.model.McpOAuthAuthorizationStartedException
import ru.sapozhnikov.aiagent.domain.model.McpServerConfig
import ru.sapozhnikov.aiagent.domain.model.McpServerConnection
import ru.sapozhnikov.aiagent.domain.repository.McpToolRepository
import ru.sapozhnikov.aiagent.domain.repository.SettingsRepository
import java.util.UUID
import javax.inject.Inject

/** Use-case для управления настройками приложения. */
internal class SettingsInteractor @Inject constructor(
    private val repository: SettingsRepository,
    private val mcpToolRepository: McpToolRepository,
    private val oauthSessionManager: McpOAuthSessionManager,
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
            mcpToolRepository.disconnectAll()
        }
    }

    fun observeMcpServers(): Flow<List<McpServerConfig>> = repository.observeMcpServers()

    suspend fun getMcpServers(): List<McpServerConfig> = repository.getMcpServers()

    suspend fun addMcpServer(
        url: String,
        authType: McpAuthType,
        authToken: String = "",
    ): McpServerConfig {
        val server = McpServerConfig(
            id = UUID.randomUUID().toString(),
            url = url.trim(),
            authType = authType,
            authToken = authToken.trim(),
            enabled = true,
        )
        repository.addMcpServer(server)
        return server
    }

    suspend fun updateMcpServer(server: McpServerConfig) {
        repository.updateMcpServer(server)
    }

    suspend fun deleteMcpServer(serverId: String) {
        mcpToolRepository.disconnect(serverId)
        repository.removeMcpServer(serverId)
    }

    suspend fun setMcpServerEnabled(serverId: String, enabled: Boolean) {
        val server = repository.getMcpServers().firstOrNull { it.id == serverId } ?: return
        repository.updateMcpServer(server.copy(enabled = enabled))
        if (!enabled) {
            mcpToolRepository.disconnect(serverId)
        }
    }

    suspend fun connectToMcpServer(server: McpServerConfig): Result<McpServerConnection> {
        if (server.url.isBlank()) {
            return Result.failure(IllegalArgumentException("Укажите URL MCP-сервера"))
        }

        val preparedServer = when (server.authType) {
            McpAuthType.OAUTH -> oauthSessionManager.refreshTokensIfNeeded(server)
            else -> server
        }

        if (preparedServer.authType == McpAuthType.OAUTH && preparedServer.resolveAccessToken() == null) {
            return startMcpOAuthAuthorization(preparedServer)
                .map { throw McpOAuthAuthorizationStartedException() }
        }

        return mcpToolRepository.connect(
            serverId = preparedServer.id,
            serverUrl = preparedServer.url,
            authToken = preparedServer.resolveAccessToken(),
        ).onSuccess { connection ->
            repository.updateMcpServer(
                preparedServer.copy(
                    serverName = connection.serverName,
                    serverVersion = connection.serverVersion,
                    tools = connection.tools,
                ),
            )
        }.recoverCatching { error ->
            if (preparedServer.authType == McpAuthType.OAUTH) {
                startMcpOAuthAuthorization(preparedServer).getOrThrow()
                throw McpOAuthAuthorizationStartedException()
            }
            throw error
        }
    }

    suspend fun startMcpOAuthAuthorization(server: McpServerConfig): Result<Unit> = runCatching {
        val request = oauthSessionManager.startAuthorization(server)
        oauthSessionManager.openAuthorizationPage(request.authorizationUrl)
    }

    suspend fun disconnectFromMcpServer(serverId: String) {
        mcpToolRepository.disconnect(serverId)
        val server = repository.getMcpServers().firstOrNull { it.id == serverId } ?: return
        repository.updateMcpServer(
            server.copy(
                serverName = null,
                serverVersion = null,
                tools = emptyList(),
            ),
        )
    }

    suspend fun connectAllEnabledMcpServers(): List<Result<McpServerConnection>> {
        val servers = repository.getMcpServers().filter { it.enabled }
        return servers.map { server ->
            connectToMcpServer(server)
        }
    }
}
