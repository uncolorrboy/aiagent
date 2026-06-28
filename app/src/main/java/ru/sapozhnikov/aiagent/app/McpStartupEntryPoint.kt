package ru.sapozhnikov.aiagent.app

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface McpStartupEntryPoint {
    fun mcpStartupConnector(): McpStartupConnector
}
