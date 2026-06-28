package ru.sapozhnikov.aiagent.domain.interactor

import ru.sapozhnikov.aiagent.domain.model.AiAgentMessage
import ru.sapozhnikov.aiagent.domain.model.ApiConversationContext
import ru.sapozhnikov.aiagent.domain.repository.AiAgentRepository
import ru.sapozhnikov.aiagent.domain.repository.McpToolRepository
import ru.sapozhnikov.aiagent.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Оркестратор запросов к LLM: при включённом MCP передаёт инструменты серверов в DeepSeek
 * и выполняет tool calls через MCP.
 */
internal class AgentOrchestratorInteractor @Inject constructor(
    private val aiAgentRepository: AiAgentRepository,
    private val mcpToolRepository: McpToolRepository,
    private val settingsRepository: SettingsRepository,
    private val settingsInteractor: SettingsInteractor,
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

        if (!ensureMcpConnections()) {
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

    private suspend fun ensureMcpConnections(): Boolean {
        val enabledServers = settingsRepository.getMcpServers().filter { it.enabled }
        if (enabledServers.isEmpty()) return false

        val cachedConnections = mcpToolRepository.getCachedConnections()
        val missingServers = enabledServers.filter { server ->
            cachedConnections[server.id] == null
        }
        if (missingServers.isEmpty()) return true

        val results = missingServers.map { server ->
            settingsInteractor.connectToMcpServer(server)
        }
        return results.any { it.isSuccess }
    }
}
