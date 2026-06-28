package ru.sapozhnikov.aiagent.data.remote.mcp

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.sapozhnikov.aiagent.data.remote.dto.FunctionDefinitionDto
import ru.sapozhnikov.aiagent.data.remote.dto.ToolDto
import ru.sapozhnikov.aiagent.domain.model.McpToolDefinition
import ru.sapozhnikov.aiagent.domain.model.McpToolNaming
import javax.inject.Inject
import javax.inject.Singleton

/** Преобразует MCP-инструменты в формат DeepSeek tools API. */
@Singleton
internal class McpToolMapper @Inject constructor() {

    private val gson = Gson()
    private val mapType = object : TypeToken<Map<String, Any?>>() {}.type

    fun toToolDto(apiName: String, tool: McpToolDefinition): ToolDto {
        val parameters = tool.inputSchemaJson?.let { schemaJson ->
            runCatching {
                gson.fromJson<Map<String, Any?>>(schemaJson, mapType)
            }.getOrNull()
        } ?: emptyMap()

        return ToolDto(
            function = FunctionDefinitionDto(
                name = apiName,
                description = tool.description,
                parameters = parameters,
            ),
        )
    }

    fun toToolDtos(registeredTools: List<McpToolNaming.RegisteredTool>): List<ToolDto> {
        return registeredTools.map { registered -> toToolDto(registered.apiName, registered.tool) }
    }
}
