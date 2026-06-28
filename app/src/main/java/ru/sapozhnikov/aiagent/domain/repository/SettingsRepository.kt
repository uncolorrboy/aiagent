package ru.sapozhnikov.aiagent.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy
import ru.sapozhnikov.aiagent.domain.model.McpServerConfig

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

    fun observeMcpServers(): Flow<List<McpServerConfig>>

    suspend fun getMcpServers(): List<McpServerConfig>

    suspend fun saveMcpServers(servers: List<McpServerConfig>)

    suspend fun updateMcpServer(server: McpServerConfig)

    suspend fun addMcpServer(server: McpServerConfig)

    suspend fun removeMcpServer(serverId: String)
}
