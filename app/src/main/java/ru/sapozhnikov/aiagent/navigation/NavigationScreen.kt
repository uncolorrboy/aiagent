package ru.sapozhnikov.aiagent.navigation

import ru.sapozhnikov.aiagent.navigation.Screen.Chat.CONVERSATION_ID_ARG
import ru.sapozhnikov.aiagent.navigation.Screen.Chat.ROUTE
import ru.sapozhnikov.aiagent.navigation.Screen.MemoryEditor.INSTANCE_ID_ARG
import ru.sapozhnikov.aiagent.navigation.Screen.MemoryEditor.MEMORY_TYPE_ARG
import ru.sapozhnikov.aiagent.navigation.Screen.MemoryEditor.NEW_INSTANCE_ID
import ru.sapozhnikov.aiagent.navigation.Screen.MemoryEditor.ROUTE
import ru.sapozhnikov.aiagent.navigation.Screen.Task.ROUTE


/**
 * Описание экранов приложения и их маршрутов навигации.
 */
sealed class Screen(val route: String) {

    /** Список сохранённых диалогов. */
    object ChatList : Screen("chat_list")

    /** Экран настроек приложения. */
    object Settings : Screen("settings")

    /** Экран управления MCP-серверами. */
    object McpServers : Screen("mcp_servers")

    /**
     * Экран создания или редактирования MCP-сервера.
     */
    object McpServerEditor : Screen("mcp_server_editor/{serverId}") {
        const val ROUTE = "mcp_server_editor/{serverId}"
        const val SERVER_ID_ARG = "serverId"
        const val NEW_SERVER_ID = "new"

        fun createRoute(serverId: String? = null): String {
            val id = serverId ?: NEW_SERVER_ID
            return "mcp_server_editor/$id"
        }
    }

    /**
     * Экран задачи с этапами.
     *
     * @property ROUTE шаблон маршрута с аргументом [Chat.CONVERSATION_ID_ARG]
     */
    object Task : Screen("task/{conversationId}") {
        const val ROUTE = "task/{conversationId}"

        fun createRoute(conversationId: String) = "task/$conversationId"
    }

    /**
     * Экран создания или редактирования экземпляра памяти.
     *
     * @property ROUTE шаблон маршрута с аргументами [MEMORY_TYPE_ARG] и [INSTANCE_ID_ARG]
     * @property MEMORY_TYPE_ARG тип памяти — [ru.sapozhnikov.aiagent.presentation.settings.MemoryEditorType]
     * @property INSTANCE_ID_ARG идентификатор экземпляра или [NEW_INSTANCE_ID] для создания
     */
    object MemoryEditor : Screen("memory_editor/{memoryType}/{instanceId}") {
        const val ROUTE = "memory_editor/{memoryType}/{instanceId}"
        const val MEMORY_TYPE_ARG = "memoryType"
        const val INSTANCE_ID_ARG = "instanceId"
        const val NEW_INSTANCE_ID = "new"

        fun createRoute(
            memoryType: String,
            instanceId: String? = null,
        ): String {
            val id = instanceId ?: NEW_INSTANCE_ID
            return "memory_editor/$memoryType/$id"
        }
    }

    /**
     * Экран создания или редактирования инварианта.
     */
    object InvariantEditor : Screen("invariant_editor/{invariantId}") {
        const val ROUTE = "invariant_editor/{invariantId}"
        const val INVARIANT_ID_ARG = "invariantId"
        const val NEW_INVARIANT_ID = "new"

        fun createRoute(invariantId: String? = null): String {
            val id = invariantId ?: NEW_INVARIANT_ID
            return "invariant_editor/$id"
        }
    }

    /**
     * Экран чата с конкретным диалогом.
     *
     * @property ROUTE шаблон маршрута с аргументом [CONVERSATION_ID_ARG]
     * @property CONVERSATION_ID_ARG имя аргумента навигации — идентификатор диалога
     */
    object Chat : Screen("chat/{conversationId}") {
        const val ROUTE = "chat/{conversationId}"
        const val CONVERSATION_ID_ARG = "conversationId"

        /** Формирует маршрут для перехода к диалогу с заданным [conversationId]. */
        fun createRoute(conversationId: String) = "chat/$conversationId"
    }
}
