package ru.sapozhnikov.aiagent.domain.model

/**
 * Описание MCP-инструмента, полученного от сервера.
 *
 * @property name уникальное имя инструмента
 * @property description человекочитаемое описание
 * @property inputSchemaJson JSON Schema входных параметров
 */
internal data class McpToolDefinition(
    val name: String,
    val description: String?,
    val inputSchemaJson: String?,
)

/**
 * Результат успешного подключения к MCP-серверу.
 *
 * @property serverName имя сервера из MCP initialize
 * @property serverVersion версия сервера
 * @property tools список доступных инструментов
 */
internal data class McpServerConnection(
    val serverName: String,
    val serverVersion: String?,
    val tools: List<McpToolDefinition>,
)
