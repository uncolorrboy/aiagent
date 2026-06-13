package ru.sapozhnikov.aiagent.domain.interactor

import kotlinx.coroutines.flow.Flow
import ru.sapozhnikov.aiagent.domain.repository.SettingsRepository
import javax.inject.Inject

internal class SettingsInteractor @Inject constructor(
    private val repository: SettingsRepository,
) {

    fun observeContextManagementEnabled(): Flow<Boolean> {
        return repository.observeContextManagementEnabled()
    }

    suspend fun setContextManagementEnabled(enabled: Boolean) {
        repository.setContextManagementEnabled(enabled)
    }
}
