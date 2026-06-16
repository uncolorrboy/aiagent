package ru.sapozhnikov.aiagent.domain.model

import ru.sapozhnikov.aiagent.domain.model.AssistantMemoryType.LONG_TERM
import ru.sapozhnikov.aiagent.domain.model.AssistantMemoryType.SHORT_TERM
import ru.sapozhnikov.aiagent.domain.model.AssistantMemoryType.WORKING


/**
 * Типы памяти ассистента.
 *
 * - [SHORT_TERM] — краткосрочная: текущий диалог (сообщения в истории чата).
 * - [WORKING] — рабочая: данные текущей задачи (экземпляры в настройках).
 * - [LONG_TERM] — долговременная: профиль, решения, знания (экземпляры профиля в настройках).
 */
internal enum class AssistantMemoryType {
    SHORT_TERM,
    WORKING,
    LONG_TERM,
}
