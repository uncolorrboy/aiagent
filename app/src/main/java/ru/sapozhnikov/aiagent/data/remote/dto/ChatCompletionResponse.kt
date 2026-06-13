package ru.sapozhnikov.aiagent.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Ответ DeepSeek Chat Completions API.
 *
 * @property choices варианты ответа модели
 * @property usage статистика использования токенов
 */
data class ChatCompletionResponse(
    @SerializedName("choices")
    val choices: List<ChatChoiceDto>,
    @SerializedName("usage")
    val usage: UsageDto?,
)

/**
 * Вариант ответа модели.
 *
 * @property message текст ответа ассистента
 */
data class ChatChoiceDto(
    @SerializedName("message")
    val message: ChatMessageDto,
)

/**
 * Статистика использования токенов в ответе API.
 *
 * @property promptTokens число токенов промпта
 * @property completionTokens число токенов в ответе
 * @property totalTokens суммарное число токенов
 * @property promptCacheHitTokens токены промпта из кэша
 * @property promptCacheMissTokens токены промпта вне кэша
 */
data class UsageDto(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int,
    @SerializedName("prompt_cache_hit_tokens")
    val promptCacheHitTokens: Int,
    @SerializedName("prompt_cache_miss_tokens")
    val promptCacheMissTokens: Int,
)
