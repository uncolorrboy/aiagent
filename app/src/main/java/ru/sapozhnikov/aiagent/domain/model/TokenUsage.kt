package ru.sapozhnikov.aiagent.domain.model

internal data class TokenUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val promptCacheHitTokens: Int,
    val promptCacheMissTokens: Int,
)
