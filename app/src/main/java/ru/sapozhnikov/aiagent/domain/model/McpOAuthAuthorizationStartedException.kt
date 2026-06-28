package ru.sapozhnikov.aiagent.domain.model

/** Сигнализирует, что для подключения запущена OAuth-авторизация в браузере. */
internal class McpOAuthAuthorizationStartedException :
    Exception("Открыта страница OAuth-авторизации")
