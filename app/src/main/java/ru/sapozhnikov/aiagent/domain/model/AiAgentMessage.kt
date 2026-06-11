package ru.sapozhnikov.aiagent.domain.model

internal data class AiAgentMessage(
    val text: String,
    val usage: TokenUsage,
)
