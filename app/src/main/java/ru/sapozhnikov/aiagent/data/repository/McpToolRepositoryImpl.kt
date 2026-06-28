package ru.sapozhnikov.aiagent.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.request.header
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.StreamableHttpClientTransport
import io.modelcontextprotocol.kotlin.sdk.types.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.types.CallToolRequestParams
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import ru.sapozhnikov.aiagent.data.remote.mcp.McpToolMapper
import ru.sapozhnikov.aiagent.domain.model.McpServerConnection
import ru.sapozhnikov.aiagent.domain.model.McpToolDefinition
import ru.sapozhnikov.aiagent.domain.model.McpToolNaming
import ru.sapozhnikov.aiagent.domain.repository.McpToolRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Реализация [McpToolRepository] через MCP Kotlin SDK (Streamable HTTP). */
@Singleton
internal class McpToolRepositoryImpl @Inject constructor(
    private val mcpToolMapper: McpToolMapper,
) : McpToolRepository {

    private val mutex = Mutex()
    private val connections = mutableMapOf<String, ActiveConnection>()

    override suspend fun connect(
        serverId: String,
        serverUrl: String,
        authToken: String?,
    ): Result<McpServerConnection> = runCatching {
        mutex.withLock {
            disconnectInternal(serverId)

            val client = HttpClient(OkHttp) {
                install(SSE)
            }

            val mcp = Client(
                clientInfo = Implementation(
                    name = "aiagent-android",
                    version = "1.0.0",
                ),
            )
            val transport = StreamableHttpClientTransport(
                client = client,
                url = serverUrl.trim(),
            ) {
                authToken?.trim()?.takeIf { it.isNotEmpty() }?.let { token ->
                    header("Authorization", "Bearer $token")
                }
            }

            mcp.connect(transport)

            val serverInfo = mcp.serverVersion
                ?: throw IllegalStateException("MCP-сервер не вернул информацию о себе")

            val tools = mcp.listTools().tools.map { tool ->
                McpToolDefinition(
                    name = tool.name,
                    description = tool.description,
                    inputSchemaJson = Json.encodeToString(ToolSchema.serializer(), tool.inputSchema),
                )
            }

            val registeredTools = McpToolNaming.registerServerTools(serverId, tools)
            val connection = McpServerConnection(
                serverId = serverId,
                serverName = serverInfo.name,
                serverVersion = serverInfo.version,
                tools = tools,
            )
            connections[serverId] = ActiveConnection(
                httpClient = client,
                mcpClient = mcp,
                connection = connection,
                registeredTools = registeredTools,
            )
            connection
        }
    }

    override suspend fun disconnect(serverId: String) {
        mutex.withLock {
            disconnectInternal(serverId)
        }
    }

    override suspend fun disconnectAll() {
        mutex.withLock {
            connections.keys.toList().forEach { serverId ->
                disconnectInternal(serverId)
            }
        }
    }

    override suspend fun listTools(serverId: String): Result<List<McpToolDefinition>> = runCatching {
        val mcp = connections[serverId]?.mcpClient
            ?: throw IllegalStateException("MCP-сервер не подключён")
        mcp.listTools().tools.map { tool ->
            McpToolDefinition(
                name = tool.name,
                description = tool.description,
                inputSchemaJson = Json.encodeToString(ToolSchema.serializer(), tool.inputSchema),
            )
        }
    }

    override suspend fun callTool(apiToolName: String, argumentsJson: String): Result<String> = runCatching {
        val (serverId, toolName) = McpToolNaming.parseApiName(apiToolName)
            ?: throw IllegalArgumentException("Неизвестный инструмент: $apiToolName")
        val mcp = connections[serverId]?.mcpClient
            ?: throw IllegalStateException("MCP-сервер не подключён")
        val arguments = if (argumentsJson.isBlank()) {
            buildJsonObject { }
        } else {
            Json.parseToJsonElement(argumentsJson) as JsonObject
        }
        val result = mcp.callTool(
            CallToolRequest(
                CallToolRequestParams(
                    name = toolName,
                    arguments = arguments,
                ),
            ),
        )
        result.content
            .filterIsInstance<TextContent>()
            .joinToString("\n") { it.text ?: "" }
    }

    override fun getCachedConnections(): Map<String, McpServerConnection> {
        return connections.mapValues { it.value.connection }
    }

    override fun getToolDtos() = connections.values.flatMap { activeConnection ->
        mcpToolMapper.toToolDtos(activeConnection.registeredTools)
    }

    private suspend fun disconnectInternal(serverId: String) {
        connections.remove(serverId)?.let { activeConnection ->
            McpToolNaming.unregisterServer(serverId)
            runCatching { activeConnection.mcpClient.close() }
            runCatching { activeConnection.httpClient.close() }
        }
    }

    private data class ActiveConnection(
        val httpClient: HttpClient,
        val mcpClient: Client,
        val connection: McpServerConnection,
        val registeredTools: List<McpToolNaming.RegisteredTool>,
    )
}
