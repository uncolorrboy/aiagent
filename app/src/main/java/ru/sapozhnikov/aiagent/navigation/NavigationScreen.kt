package ru.sapozhnikov.aiagent.navigation

sealed class Screen(val route: String) {
    object Chat: Screen("main_screen")
}