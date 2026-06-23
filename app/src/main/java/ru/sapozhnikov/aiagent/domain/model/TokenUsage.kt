package ru.sapozhnikov.aiagent.domain.model

/**
 * Статистика использования токенов в одном запросе к LLM API.
 *
 * @property promptTokens общее число токенов промпта
 * @property completionTokens число токенов в ответе модели
 * @property totalTokens суммарное число токенов
 * @property promptCacheHitTokens токены промпта, попавшие в кэш
 * @property promptCacheMissTokens токены промпта, не попавшие в кэш
 */
internal data class TokenUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val promptCacheHitTokens: Int,
    val promptCacheMissTokens: Int,
) {
    operator fun plus(other: TokenUsage): TokenUsage {
        return TokenUsage(
            promptTokens = promptTokens + other.promptTokens,
            completionTokens = completionTokens + other.completionTokens,
            totalTokens = totalTokens + other.totalTokens,
            promptCacheHitTokens = promptCacheHitTokens + other.promptCacheHitTokens,
            promptCacheMissTokens = promptCacheMissTokens + other.promptCacheMissTokens,
        )
    }

    companion object {
        fun zero() = TokenUsage(0, 0, 0, 0, 0)
    }
}
