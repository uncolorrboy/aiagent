package ru.sapozhnikov.aiagent.domain.model

internal data class SavedUserFileMessage(
    val content: String,
    val fileName: String,
)
