package ru.sapozhnikov.aiagent.domain.model

/** Кодирование имён MCP-инструментов для LLM API с несколькими серверами. */
internal object McpToolNaming {

    private val apiNamePattern = Regex("^[a-zA-Z0-9_-]+$")
    private val registry = mutableMapOf<String, ToolTarget>()

    data class ToolTarget(
        val serverId: String,
        val toolName: String,
    )

    data class RegisteredTool(
        val apiName: String,
        val tool: McpToolDefinition,
    )

    /** Регистрирует инструменты сервера и возвращает безопасные имена для LLM API. */
    fun registerServerTools(serverId: String, tools: List<McpToolDefinition>): List<RegisteredTool> {
        unregisterServer(serverId)

        val usedNames = registry.keys.toMutableSet()
        return tools.map { tool ->
            val apiName = generateApiName(serverId, tool.name, usedNames)
            registry[apiName] = ToolTarget(serverId = serverId, toolName = tool.name)
            usedNames.add(apiName)
            RegisteredTool(apiName = apiName, tool = tool)
        }
    }

    /** Удаляет инструменты сервера из реестра. */
    fun unregisterServer(serverId: String) {
        registry.entries.removeIf { it.value.serverId == serverId }
    }

    /** Разрешает имя инструмента из LLM API в пару (serverId, toolName). */
    fun parseApiName(apiName: String): Pair<String, String>? {
        val target = registry[apiName] ?: return null
        return target.serverId to target.toolName
    }

    private fun generateApiName(
        serverId: String,
        toolName: String,
        usedNames: Set<String>,
    ): String {
        val serverPart = serverId.filter { it.isLetterOrDigit() }.take(8).ifEmpty { "server" }
        val toolPart = toolName
            .map { char -> if (char.isLetterOrDigit() || char == '_' || char == '-') char else '_' }
            .joinToString("")
            .trim('_')
            .take(40)
            .ifEmpty { "tool" }

        var candidate = "mcp_${serverPart}_$toolPart"
        var suffix = 0
        while (!isValidApiName(candidate) || candidate in usedNames) {
            candidate = "mcp_${serverPart}_${toolPart}_$suffix"
            suffix++
        }
        return candidate
    }

    private fun isValidApiName(name: String): Boolean = apiNamePattern.matches(name)
}
