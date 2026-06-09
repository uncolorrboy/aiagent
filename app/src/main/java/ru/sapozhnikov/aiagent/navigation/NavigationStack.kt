package ru.sapozhnikov.aiagent.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.sapozhnikov.aiagent.presentation.chat.ChatRoot

@Composable
internal fun NavigationStack(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Chat.route,
        modifier = modifier,
    ) {
        composable(route = Screen.Chat.route) {
            ChatRoot()
        }
    }
}
