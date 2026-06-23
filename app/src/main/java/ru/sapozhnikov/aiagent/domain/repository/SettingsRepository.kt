package ru.sapozhnikov.aiagent.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy
import ru.sapozhnikov.aiagent.domain.model.McpToolDefinition

/** Репозиторий пользовательских настроек приложения. */
internal interface SettingsRepository {

    /** Наблюдает за выбранной стратегией управления контекстом. */
    fun observeContextManagementStrategy(): Flow<ContextManagementStrategy>

    /** Возвращает текущую стратегию управления контекстом. */
    suspend fun getContextManagementStrategy(): ContextManagementStrategy

    /** Сохраняет выбранную стратегию управления контекстом. */
    suspend fun setContextManagementStrategy(strategy: ContextManagementStrategy)

    fun observeMcpEnabled(): Flow<Boolean>

    suspend fun setMcpEnabled(enabled: Boolean)

    suspend fun isMcpEnabled(): Boolean

    fun observeMcpServerUrl(): Flow<String>

    suspend fun getMcpServerUrl(): String

    suspend fun getMcpAuthToken(): String

    suspend fun setMcpServerUrl(url: String)

    fun observeMcpAuthToken(): Flow<String>

    suspend fun setMcpAuthToken(token: String)

    fun observeMcpServerName(): Flow<String?>

    fun observeMcpServerVersion(): Flow<String?>

    fun observeMcpTools(): Flow<List<McpToolDefinition>>

    suspend fun saveMcpConnectionInfo(
        serverName: String,
        serverVersion: String?,
        tools: List<McpToolDefinition>,
    )

    suspend fun clearMcpConnectionInfo()
}
