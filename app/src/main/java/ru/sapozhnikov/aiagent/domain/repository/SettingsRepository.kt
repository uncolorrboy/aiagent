package ru.sapozhnikov.aiagent.domain.repository

import kotlinx.coroutines.flow.Flow

internal interface SettingsRepository {

    fun observeContextManagementEnabled(): Flow<Boolean>

    suspend fun isContextManagementEnabled(): Boolean

    suspend fun setContextManagementEnabled(enabled: Boolean)
}
