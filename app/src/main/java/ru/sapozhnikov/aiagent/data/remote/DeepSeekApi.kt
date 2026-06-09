package ru.sapozhnikov.aiagent.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import ru.sapozhnikov.aiagent.data.remote.dto.ChatCompletionRequest
import ru.sapozhnikov.aiagent.data.remote.dto.ChatCompletionResponse

internal interface DeepSeekApi {

    @POST("chat/completions")
    suspend fun createChatCompletion(@Body request: ChatCompletionRequest): ChatCompletionResponse
}
