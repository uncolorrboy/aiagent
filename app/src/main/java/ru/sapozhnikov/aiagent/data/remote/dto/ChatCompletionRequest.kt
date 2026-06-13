package ru.sapozhnikov.aiagent.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Тело запроса к DeepSeek Chat Completions API.
 *
 * @property model идентификатор модели
 * @property messages история сообщений для completion
 * @property thinking параметры режима «размышления»
 * @property stream включить ли потоковую передачу ответа
 */
data class ChatCompletionRequest(
    @SerializedName("model")
    val model: String,
    @SerializedName("messages")
    val messages: List<ChatMessageDto>,
    @SerializedName("thinking")
    val thinking: ThinkingDto,
    @SerializedName("stream")
    val stream: Boolean = false
)

/**
 * Сообщение в формате Chat Completions API.
 *
 * @property role роль отправителя (system, user, assistant)
 * @property content текст сообщения
 */
data class ChatMessageDto(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: String,
)

/**
 * Параметры режима «размышления» модели.
 *
 * @property type тип режима (например, disabled)
 */
data class ThinkingDto(
    @SerializedName("type")
    val type: String,
)
