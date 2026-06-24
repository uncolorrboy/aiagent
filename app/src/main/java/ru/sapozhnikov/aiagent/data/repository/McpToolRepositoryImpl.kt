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
import ru.sapozhnikov.aiagent.domain.repository.McpToolRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Реализация [McpToolRepository] через MCP Kotlin SDK (Streamable HTTP). */
@Singleton
internal class McpToolRepositoryImpl @Inject constructor(
    private val mcpToolMapper: McpToolMapper,
) : McpToolRepository {

    private val mutex = Mutex()
    private var httpClient: HttpClient? = null
    private var mcpClient: Client? = null
    private var cachedConnection: McpServerConnection? = null

    override suspend fun connect(
        serverUrl: String,
        authToken: String?,
    ): Result<McpServerConnection> = runCatching {
        mutex.withLock {
            disconnectInternal()

            val client = HttpClient(OkHttp) {
                install(SSE)
            }
            httpClient = client

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
            mcpClient = mcp

            val serverInfo = mcp.serverVersion
                ?: throw IllegalStateException("MCP-сервер не вернул информацию о себе")

            val tools = mcp.listTools().tools.map { tool ->
                McpToolDefinition(
                    name = tool.name,
                    description = tool.description,
                    inputSchemaJson = Json.encodeToString(ToolSchema.serializer(), tool.inputSchema),
                )
            }

            McpServerConnection(
                serverName = serverInfo.name,
                serverVersion = serverInfo.version,
                tools = tools,
            ).also { cachedConnection = it }
        }
    }

    override suspend fun disconnect() {
        mutex.withLock {
            disconnectInternal()
        }
    }

    override suspend fun listTools(): Result<List<McpToolDefinition>> = runCatching {
        val mcp = mcpClient ?: throw IllegalStateException("MCP-сервер не подключён")
        mcp.listTools().tools.map { tool ->
            McpToolDefinition(
                name = tool.name,
                description = tool.description,
                inputSchemaJson = Json.encodeToString(ToolSchema.serializer(), tool.inputSchema),
            )
        }
    }

    override suspend fun callTool(name: String, argumentsJson: String): Result<String> = runCatching {
        val mcp = mcpClient ?: throw IllegalStateException("MCP-сервер не подключён")
        val arguments = if (argumentsJson.isBlank()) {
            buildJsonObject { }
        } else {
            Json.parseToJsonElement(argumentsJson) as JsonObject
        }
        val result = mcp.callTool(
            CallToolRequest(
                CallToolRequestParams(
                    name = name,
                    arguments = arguments,
                ),
            ),
        )
        result.content
            .filterIsInstance<TextContent>()
            .joinToString("\n") { it.text ?: "" }
    }

    override fun getCachedConnection(): McpServerConnection? = cachedConnection

    override fun getToolDtos() = mcpToolMapper.toToolDtos(cachedConnection?.tools.orEmpty())

    private suspend fun disconnectInternal() {
        runCatching { mcpClient?.close() }
        runCatching { httpClient?.close() }
        mcpClient = null
        httpClient = null
        cachedConnection = null
    }
}
