package ru.sapozhnikov.aiagent.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatCompletionResponse(
    @SerializedName("choices")
    val choices: List<ChatChoiceDto>,
)

data class ChatChoiceDto(
    @SerializedName("message")
    val message: ChatMessageDto,
)
