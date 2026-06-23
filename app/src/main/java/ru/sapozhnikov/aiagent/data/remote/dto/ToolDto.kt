package ru.sapozhnikov.aiagent.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Описание инструмента для DeepSeek Chat Completions API. */
data class ToolDto(
    @SerializedName("type")
    val type: String = "function",
    @SerializedName("function")
    val function: FunctionDefinitionDto,
)

/** Определение функции в формате OpenAI/DeepSeek. */
data class FunctionDefinitionDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("parameters")
    val parameters: Map<String, Any?>,
)

/** Вызов инструмента в ответе модели. */
data class ToolCallDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("type")
    val type: String = "function",
    @SerializedName("function")
    val function: ToolCallFunctionDto,
)

/** Имя и аргументы вызова функции. */
data class ToolCallFunctionDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("arguments")
    val arguments: String,
)
