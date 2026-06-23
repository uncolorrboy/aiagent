package ru.sapozhnikov.aiagent.domain.interactor

import ru.sapozhnikov.aiagent.domain.model.AiAgentMessage
import ru.sapozhnikov.aiagent.domain.model.ApiConversationContext
import ru.sapozhnikov.aiagent.domain.model.McpServerConnection
import ru.sapozhnikov.aiagent.domain.repository.AiAgentRepository
import ru.sapozhnikov.aiagent.domain.repository.McpToolRepository
import ru.sapozhnikov.aiagent.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Оркестратор запросов к LLM: при включённом MCP передаёт инструменты сервера в DeepSeek
 * и выполняет tool calls через MCP.
 */
internal class AgentOrchestratorInteractor @Inject constructor(
    private val aiAgentRepository: AiAgentRepository,
    private val mcpToolRepository: McpToolRepository,
    private val settingsRepository: SettingsRepository,
) {

    suspend fun sendMessage(
        context: ApiConversationContext,
        userMessage: String,
    ): Result<AiAgentMessage> {
        if (userMessage.isBlank()) {
            return Result.failure(IllegalArgumentException("Сообщение не может быть пустым"))
        }

        val trimmedMessage = userMessage.trim()
        if (!settingsRepository.isMcpEnabled()) {
            return aiAgentRepository.sendMessage(context, trimmedMessage)
        }

        if (ensureMcpConnection() == null) {
            return aiAgentRepository.sendMessage(context, trimmedMessage)
        }

        val tools = mcpToolRepository.getToolDtos()
        if (tools.isEmpty()) {
            return aiAgentRepository.sendMessage(context, trimmedMessage)
        }

        return aiAgentRepository.sendMessageWithTools(
            context = context,
            userMessage = trimmedMessage,
            tools = tools,
            toolExecutor = { name, argumentsJson ->
                mcpToolRepository.callTool(name, argumentsJson)
            },
        )
    }

    private suspend fun ensureMcpConnection(): McpServerConnection? {
        mcpToolRepository.getCachedConnection()?.let { return it }

        val url = settingsRepository.getMcpServerUrl()
        if (url.isBlank()) return null

        val authToken = settingsRepository.getMcpAuthToken()
        return mcpToolRepository.connect(url, authToken.takeIf { it.isNotBlank() })
            .onSuccess { connection ->
                settingsRepository.saveMcpConnectionInfo(
                    serverName = connection.serverName,
                    serverVersion = connection.serverVersion,
                    tools = connection.tools,
                )
            }
            .getOrNull()
    }
}
