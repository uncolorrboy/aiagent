package ru.sapozhnikov.aiagent.domain.repository

import kotlinx.coroutines.flow.Flow

/** Репозиторий пользовательских настроек приложения. */
internal interface SettingsRepository {

    /** Наблюдает за состоянием флага управления контекстом. */
    fun observeContextManagementEnabled(): Flow<Boolean>

    /** Возвращает текущее значение флага управления контекстом. */
    suspend fun isContextManagementEnabled(): Boolean

    /** Включает или отключает сжатие контекста через резюме. */
    suspend fun setContextManagementEnabled(enabled: Boolean)
}
