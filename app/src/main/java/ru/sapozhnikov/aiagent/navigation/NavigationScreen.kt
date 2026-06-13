package ru.sapozhnikov.aiagent.navigation

import ru.sapozhnikov.aiagent.navigation.Screen.Chat.CONVERSATION_ID_ARG
import ru.sapozhnikov.aiagent.navigation.Screen.Chat.ROUTE


/**
 * Описание экранов приложения и их маршрутов навигации.
 */
sealed class Screen(val route: String) {

    /** Список сохранённых диалогов. */
    object ChatList : Screen("chat_list")

    /** Экран настроек приложения. */
    object Settings : Screen("settings")

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
