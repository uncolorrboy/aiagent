package ru.sapozhnikov.aiagent.presentation.formatter

import java.util.Locale

/** Форматирует стоимость диалога в USD для отображения в UI. */
internal fun formatChatPrice(price: Double): String {
    return when {
        price == 0.0 -> "$0.00"
        price < 0.01 -> String.format(Locale.US, "$%.6f", price)
        else -> String.format(Locale.US, "$%.2f", price)
    }
}
