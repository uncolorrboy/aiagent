package ru.sapozhnikov.aiagent.presentation.formatter

internal fun formatTokenCount(count: Int): String {
    val word = when {
        count % 100 in 11..14 -> "токенов"
        count % 10 == 1 -> "токен"
        count % 10 in 2..4 -> "токена"
        else -> "токенов"
    }
    return "$count $word"
}
