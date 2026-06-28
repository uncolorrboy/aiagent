package ru.sapozhnikov.aiagent.app

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.sapozhnikov.aiagent.domain.interactor.SettingsInteractor
import ru.sapozhnikov.aiagent.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Подключает MCP-серверы при запуске приложения, если функция включена. */
@Singleton
internal class McpStartupConnector @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val settingsInteractor: SettingsInteractor,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun connectIfEnabled() {
        scope.launch {
            if (!settingsRepository.isMcpEnabled()) return@launch
            settingsInteractor.connectAllEnabledMcpServers()
        }
    }
}
