package ru.sapozhnikov.aiagent.domain.model

private const val MILLION = 1_000_000.0

private const val CACHE_HIT_PRICE_PER_MILLION = 0.0028
private const val CACHE_MISS_PRICE_PER_MILLION = 0.14
private const val OUTPUT_PRICE_PER_MILLION = 0.28

internal fun calculateChatCost(messages: List<ChatHistoryMessage>): Double {
    return messages.sumOf { message ->
        when (message.role) {
            MessageRole.USER -> {
                val cacheHit = message.cacheHitTokens ?: 0
                val cacheMiss = message.tokenCount ?: 0
                cacheHit * CACHE_HIT_PRICE_PER_MILLION / MILLION +
                    cacheMiss * CACHE_MISS_PRICE_PER_MILLION / MILLION
            }
            MessageRole.AI -> {
                val outputTokens = message.tokenCount ?: 0
                outputTokens * OUTPUT_PRICE_PER_MILLION / MILLION
            }
        }
    }
}
