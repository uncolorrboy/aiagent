package ru.sapozhnikov.aiagent.domain.repository

import ru.sapozhnikov.aiagent.data.remote.dto.ToolDto
import ru.sapozhnikov.aiagent.domain.model.McpServerConnection
import ru.sapozhnikov.aiagent.domain.model.McpToolDefinition

/** Репозиторий для подключения к MCP-серверам и работы с инструментами. */
internal interface McpToolRepository {

    /** Подключается к MCP-серверу и возвращает информацию о сервере и инструментах. */
    suspend fun connect(
        serverId: String,
        serverUrl: String,
        authToken: String?,
    ): Result<McpServerConnection>

    /** Отключается от MCP-сервера с заданным [serverId]. */
    suspend fun disconnect(serverId: String)

    /** Отключается от всех MCP-серверов. */
    suspend fun disconnectAll()

    /** Возвращает список инструментов с активного подключения [serverId]. */
    suspend fun listTools(serverId: String): Result<List<McpToolDefinition>>

    /** Вызывает инструмент на MCP-сервере по API-имени (serverId::toolName). */
    suspend fun callTool(apiToolName: String, argumentsJson: String): Result<String>

    /** Возвращает кэшированные подключения по идентификаторам серверов. */
    fun getCachedConnections(): Map<String, McpServerConnection>

    /** Преобразует инструменты всех подключений в формат DeepSeek tools API. */
    fun getToolDtos(): List<ToolDto>
}
