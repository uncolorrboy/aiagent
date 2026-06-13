package ru.sapozhnikov.aiagent.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy

/** Репозиторий пользовательских настроек приложения. */
internal interface SettingsRepository {

    /** Наблюдает за выбранной стратегией управления контекстом. */
    fun observeContextManagementStrategy(): Flow<ContextManagementStrategy>

    /** Возвращает текущую стратегию управления контекстом. */
    suspend fun getContextManagementStrategy(): ContextManagementStrategy

    /** Сохраняет выбранную стратегию управления контекстом. */
    suspend fun setContextManagementStrategy(strategy: ContextManagementStrategy)
}
