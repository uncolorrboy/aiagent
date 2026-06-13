package ru.sapozhnikov.aiagent.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.sapozhnikov.aiagent.presentation.chat.ChatRoot
import ru.sapozhnikov.aiagent.presentation.chatlist.ChatListRoot
import ru.sapozhnikov.aiagent.presentation.settings.SettingsRoot
import java.util.UUID

/**
 * Корневой навигационный граф приложения.
 *
 * Стартовый экран — список чатов; поддерживаются переходы к чату, настройкам и обратно.
 *
 * @param modifier модификатор для [NavHost]
 */
@Composable
internal fun NavigationStack(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.ChatList.route,
        modifier = modifier,
    ) {
        composable(route = Screen.ChatList.route) {
            ChatListRoot(
                onConversationClick = { conversationId ->
                    navController.navigate(Screen.Chat.createRoute(conversationId))
                },
                onNewChatClick = {
                    navController.navigate(Screen.Chat.createRoute(UUID.randomUUID().toString()))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsRoot(
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.Chat.ROUTE,
            arguments = listOf(
                navArgument(Screen.Chat.CONVERSATION_ID_ARG) { type = NavType.StringType },
            ),
        ) {
            ChatRoot(
                onOpenChatList = { navController.popBackStack() },
            )
        }
    }
}
