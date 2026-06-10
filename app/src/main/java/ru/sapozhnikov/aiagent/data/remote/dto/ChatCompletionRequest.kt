package ru.sapozhnikov.aiagent.data.remote.dto

import com.google.gson.annotations.SerializedName

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

data class ChatMessageDto(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: String,
)

data class ThinkingDto(
    @SerializedName("type")
    val type: String,
)
