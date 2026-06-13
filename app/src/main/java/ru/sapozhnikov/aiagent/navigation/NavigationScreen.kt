package ru.sapozhnikov.aiagent.navigation

sealed class Screen(val route: String) {
    object ChatList : Screen("chat_list")

    object Settings : Screen("settings")

    object Chat : Screen("chat/{conversationId}") {
        const val ROUTE = "chat/{conversationId}"
        const val CONVERSATION_ID_ARG = "conversationId"

        fun createRoute(conversationId: String) = "chat/$conversationId"
    }
}