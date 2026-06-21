package ru.sapozhnikov.aiagent.presentation.chat

import ru.sapozhnikov.aiagent.domain.model.AssistantInvariant
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy
import ru.sapozhnikov.aiagent.domain.model.ConversationMode
import ru.sapozhnikov.aiagent.domain.model.MemoryInstance
import ru.sapozhnikov.aiagent.domain.model.TaskStage
import ru.sapozhnikov.aiagent.domain.model.TaskStageTransitions

/**
 * Состояние UI экрана чата.
 *
 * @property isLoading идёт ли сейчас запрос к LLM
 * @property items сообщения в ленте чата
 * @property sendButtonState состояние кнопки отправки
 * @property totalTokenCount суммарное число токенов последнего запроса
 * @property chatPrice расчётная стоимость диалога в USD
 * @property contextStrategy активная стратегия управления контекстом
 * @property branches список веток диалога (для Branching)
 * @property canCreateCheckpoint можно ли создать checkpoint
 * @property isMemorySheetVisible открыт ли BottomSheet выбора памяти
 * @property workingMemoryInstances доступные экземпляры рабочей памяти
 * @property profileMemoryInstances доступные экземпляры долговременной памяти
 * @property selectedWorkingMemoryId выбранная рабочая память или null
 * @property selectedProfileMemoryId выбранный профиль или null
 * @property isMemorySelectionLocked заблокирован ли выбор памяти (уже сохранён для диалога)
 * @property isInvariantsSheetVisible открыт ли BottomSheet выбора инвариантов
 * @property availableInvariants доступные инварианты
 * @property selectedInvariantIds выбранные для диалога инварианты
 * @property mode режим диалога
 * @property isTaskMode активен ли режим задачи
 * @property activeTaskStage текущий активный этап задачи
 * @property viewingTaskStage этап, переписку которого просматривает пользователь
 * @property canAdvanceTaskStage можно ли перейти на следующий этап вручную
 * @property advanceTaskStageLabel текст кнопки ручного перехода
 * @property canRevertTaskStage можно ли вернуться на предыдущий этап вручную
 * @property revertTaskStageLabel текст кнопки возврата
 * @property pendingTaskTransition отложенный переход по маркеру ассистента
 */
internal data class ChatScreenUiState(
    val isLoading: Boolean = false,
    val items: List<ChatMessage> = listOf(),
    val sendButtonState: SendButtonState = SendButtonState.DISABLED,
    val totalTokenCount: Int = 0,
    val chatPrice: Double = 0.0,
    val contextStrategy: ContextManagementStrategy = ContextManagementStrategy.DEFAULT,
    val branches: List<ChatBranchUi> = emptyList(),
    val canCreateCheckpoint: Boolean = false,
    val isMemorySheetVisible: Boolean = false,
    val workingMemoryInstances: List<MemoryInstance> = emptyList(),
    val profileMemoryInstances: List<MemoryInstance> = emptyList(),
    val selectedWorkingMemoryId: String? = null,
    val selectedProfileMemoryId: String? = null,
    val isMemorySelectionLocked: Boolean = false,
    val isInvariantsSheetVisible: Boolean = false,
    val availableInvariants: List<AssistantInvariant> = emptyList(),
    val selectedInvariantIds: Set<String> = emptySet(),
    val mode: ConversationMode = ConversationMode.CHAT,
    val isTaskMode: Boolean = false,
    val activeTaskStage: TaskStage? = null,
    val viewingTaskStage: TaskStage? = null,
    val isInputEnabled: Boolean = true,
    val canAdvanceTaskStage: Boolean = false,
    val advanceTaskStageLabel: String? = null,
    val canRevertTaskStage: Boolean = false,
    val revertTaskStageLabel: String? = null,
    val pendingTaskTransition: PendingTaskStageTransition? = null,
)

/**
 * Отложенный переход между этапами задачи (после маркера ассистента).
 *
 * @property targetStage целевой этап
 * @property fromStage этап, с которого выполняется переход
 * @property progress прогресс ожидания от 0 до 1
 */
internal data class PendingTaskStageTransition(
    val targetStage: TaskStage,
    val fromStage: TaskStage,
    val progress: Float,
) {
    val isBackward: Boolean =
        TaskStageTransitions.isBackwardTransition(fromStage, targetStage)
}

/**
 * UI-модель ветки диалога.
 *
 * @property id идентификатор ветки
 * @property name отображаемое имя
 * @property isActive активна ли ветка
 */
internal data class ChatBranchUi(
    val id: String,
    val name: String,
    val isActive: Boolean,
)

/**
 * UI-модель сообщения в ленте чата.
 *
 * @property text текст сообщения или превью содержимого
 * @property time отформатированное время отправки
 * @property messageOwner отправитель — пользователь или ассистент
 * @property tokenCount число токенов (для пользовательских сообщений)
 * @property kind тип содержимого — текст или файл
 * @property attachmentFileName имя прикреплённого файла
 */
internal data class ChatMessage(
    val text: String,
    val time: String,
    val messageOwner: MessageOwner,
    val tokenCount: Int? = null,
    val kind: MessageKind = MessageKind.TEXT,
    val attachmentFileName: String? = null,
)

/** Тип содержимого сообщения в UI. */
internal enum class MessageKind {
    /** Текстовое сообщение. */
    TEXT,

    /** Сообщение с прикреплённым файлом. */
    FILE,
}

/** Отправитель сообщения в UI. */
internal enum class MessageOwner {
    /** Ответ LLM-ассистента. */
    AI,

    /** Сообщение пользователя. */
    USER,
}

/** Состояние кнопки отправки сообщения. */
internal enum class SendButtonState {
    /** Кнопка неактивна (пустой ввод). */
    DISABLED,

    /** Готова к отправке. */
    DEFAULT,

    /** Запрос к LLM выполняется. */
    IN_PROCESS,
}
