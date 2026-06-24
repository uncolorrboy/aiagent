package ru.sapozhnikov.aiagent.domain.repository

import ru.sapozhnikov.aiagent.data.remote.dto.ToolDto
import ru.sapozhnikov.aiagent.domain.model.McpServerConnection
import ru.sapozhnikov.aiagent.domain.model.McpToolDefinition

/** Репозиторий для подключения к MCP-серверу и работы с инструментами. */
internal interface McpToolRepository {

    /** Подключается к MCP-серверу и возвращает информацию о сервере и инструментах. */
    suspend fun connect(
        serverUrl: String,
        authToken: String?,
    ): Result<McpServerConnection>

    /** Отключается от текущего MCP-сервера. */
    suspend fun disconnect()

    /** Возвращает список инструментов с активного подключения. */
    suspend fun listTools(): Result<List<McpToolDefinition>>

    /** Вызывает инструмент на MCP-сервере. */
    suspend fun callTool(name: String, argumentsJson: String): Result<String>

    /** Возвращает кэшированное подключение, если оно активно. */
    fun getCachedConnection(): McpServerConnection?

    /** Преобразует инструменты текущего подключения в формат DeepSeek tools API. */
    fun getToolDtos(): List<ToolDto>
}
