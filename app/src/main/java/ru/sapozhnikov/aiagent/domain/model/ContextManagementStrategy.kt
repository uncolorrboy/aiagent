package ru.sapozhnikov.aiagent.domain.model

import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy.BRANCHING
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy.DEFAULT
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy.SLIDING_WINDOW
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy.STICKY_FACTS
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy.SUMMARY_COMPRESSION


/**
 * Стратегия управления контекстом, отправляемым в LLM API.
 *
 * [DEFAULT] — полная история без обрезки.
 * [SUMMARY_COMPRESSION] — последние 5 сообщений + резюме старой истории.
 * [SLIDING_WINDOW] — только последние N сообщений.
 * [STICKY_FACTS] — блок ключ-значение фактов + последние N сообщений.
 * [BRANCHING] — независимые ветки диалога от checkpoint.
 */
internal enum class ContextManagementStrategy {
    DEFAULT,
    SUMMARY_COMPRESSION,
    SLIDING_WINDOW,
    STICKY_FACTS,
    BRANCHING,
}
