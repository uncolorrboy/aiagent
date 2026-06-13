package ru.sapozhnikov.aiagent.domain.interactor

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.domain.repository.SettingsRepository
import javax.inject.Inject

/** Use-case для управления настройками приложения. */
internal class SettingsInteractor @Inject constructor(
    private val repository: SettingsRepository,
) {

    /** Наблюдает за состоянием флага управления контекстом. */
    fun observeContextManagementEnabled(): Flow<Boolean> {
        return repository.observeContextManagementEnabled()
    }

    /** Включает или отключает сжатие контекста через резюме. */
    suspend fun setContextManagementEnabled(enabled: Boolean) {
        repository.setContextManagementEnabled(enabled)
    }
}
