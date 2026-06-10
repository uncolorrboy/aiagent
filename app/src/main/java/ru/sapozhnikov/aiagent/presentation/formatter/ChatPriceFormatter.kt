package ru.sapozhnikov.aiagent.presentation.formatter

import java.util.Locale

internal fun formatChatPrice(price: Double): String {
    return when {
        price == 0.0 -> "$0.00"
        price < 0.01 -> String.format(Locale.US, "$%.6f", price)
        else -> String.format(Locale.US, "$%.2f", price)
    }
}
