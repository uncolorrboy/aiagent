package ru.sapozhnikov.aiagent.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.sapozhnikov.aiagent.domain.model.ContextManagementStrategy
import ru.sapozhnikov.aiagent.domain.model.McpToolDefinition
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
)

/** Хранилище пользовательских настроек на базе DataStore Preferences. */
@Singleton
internal class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val gson = com.google.gson.Gson()

    /** Поток выбранной стратегии управления контекстом. */
    val contextManagementStrategy: Flow<ContextManagementStrategy> = context.settingsDataStore.data.map { preferences ->
        resolveStrategy(preferences)
    }

    /** Поток флага включения MCP. */
    val mcpEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_MCP_ENABLED] ?: DEFAULT_MCP_ENABLED
    }

    /** Поток URL MCP-сервера. */
    val mcpServerUrl: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_MCP_SERVER_URL] ?: DEFAULT_MCP_SERVER_URL
    }

    /** Поток токена авторизации MCP-сервера. */
    val mcpAuthToken: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_MCP_AUTH_TOKEN].orEmpty()
    }

    /** Поток имени подключённого MCP-сервера. */
    val mcpServerName: Flow<String?> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_MCP_SERVER_NAME]
    }

    /** Поток версии подключённого MCP-сервера. */
    val mcpServerVersion: Flow<String?> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_MCP_SERVER_VERSION]
    }

    /** Поток списка инструментов MCP-сервера. */
    val mcpTools: Flow<List<McpToolDefinition>> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_MCP_TOOLS_JSON]?.let { json ->
            runCatching {
                gson.fromJson(json, Array<McpToolDefinitionDto>::class.java)
                    ?.map { it.toDomain() }
            }.getOrNull()
        } ?: emptyList()
    }

    /** Сохраняет выбранную стратегию управления контекстом. */
    suspend fun setContextManagementStrategy(strategy: ContextManagementStrategy) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_CONTEXT_MANAGEMENT_STRATEGY] = strategy.name
            preferences.remove(KEY_CONTEXT_MANAGEMENT_ENABLED)
        }
    }

    /** Возвращает текущую стратегию управления контекстом. */
    suspend fun getContextManagementStrategy(): ContextManagementStrategy {
        return context.settingsDataStore.data
            .map { preferences -> resolveStrategy(preferences) }
            .first()
    }

    suspend fun setMcpEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_MCP_ENABLED] = enabled
        }
    }

    suspend fun setMcpServerUrl(url: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_MCP_SERVER_URL] = url
        }
    }

    suspend fun setMcpAuthToken(token: String) {
        context.settingsDataStore.edit { preferences ->
            if (token.isBlank()) {
                preferences.remove(KEY_MCP_AUTH_TOKEN)
            } else {
                preferences[KEY_MCP_AUTH_TOKEN] = token
            }
        }
    }

    suspend fun saveMcpConnectionInfo(
        serverName: String,
        serverVersion: String?,
        tools: List<McpToolDefinition>,
    ) {
        val toolsJson = gson.toJson(tools.map { McpToolDefinitionDto.fromDomain(it) })
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_MCP_SERVER_NAME] = serverName
            if (serverVersion != null) {
                preferences[KEY_MCP_SERVER_VERSION] = serverVersion
            } else {
                preferences.remove(KEY_MCP_SERVER_VERSION)
            }
            preferences[KEY_MCP_TOOLS_JSON] = toolsJson
        }
    }

    suspend fun clearMcpConnectionInfo() {
        context.settingsDataStore.edit { preferences ->
            preferences.remove(KEY_MCP_SERVER_NAME)
            preferences.remove(KEY_MCP_SERVER_VERSION)
            preferences.remove(KEY_MCP_TOOLS_JSON)
        }
    }

    suspend fun getMcpServerUrl(): String {
        return context.settingsDataStore.data
            .map { preferences -> preferences[KEY_MCP_SERVER_URL] ?: DEFAULT_MCP_SERVER_URL }
            .first()
    }

    suspend fun getMcpAuthToken(): String {
        return context.settingsDataStore.data
            .map { preferences -> preferences[KEY_MCP_AUTH_TOKEN].orEmpty() }
            .first()
    }

    suspend fun isMcpEnabled(): Boolean {
        return context.settingsDataStore.data
            .map { preferences -> preferences[KEY_MCP_ENABLED] ?: DEFAULT_MCP_ENABLED }
            .first()
    }

    private fun resolveStrategy(preferences: Preferences): ContextManagementStrategy {
        preferences[KEY_CONTEXT_MANAGEMENT_STRATEGY]?.toContextManagementStrategy()?.let { return it }
        if (preferences[KEY_CONTEXT_MANAGEMENT_ENABLED] == true) {
            return ContextManagementStrategy.SUMMARY_COMPRESSION
        }
        return ContextManagementStrategy.DEFAULT
    }

    private fun String.toContextManagementStrategy(): ContextManagementStrategy? {
        return runCatching { ContextManagementStrategy.valueOf(this) }.getOrNull()
    }

    private data class McpToolDefinitionDto(
        val name: String,
        val description: String?,
        val inputSchemaJson: String?,
    ) {
        fun toDomain() = McpToolDefinition(
            name = name,
            description = description,
            inputSchemaJson = inputSchemaJson,
        )

        companion object {
            fun fromDomain(tool: McpToolDefinition) = McpToolDefinitionDto(
                name = tool.name,
                description = tool.description,
                inputSchemaJson = tool.inputSchemaJson,
            )
        }
    }

    private companion object {
        val KEY_CONTEXT_MANAGEMENT_STRATEGY = stringPreferencesKey("context_management_strategy")
        val KEY_CONTEXT_MANAGEMENT_ENABLED = booleanPreferencesKey("context_management_enabled")
        val KEY_MCP_ENABLED = booleanPreferencesKey("mcp_enabled")
        val KEY_MCP_SERVER_URL = stringPreferencesKey("mcp_server_url")
        val KEY_MCP_AUTH_TOKEN = stringPreferencesKey("mcp_auth_token")
        val KEY_MCP_SERVER_NAME = stringPreferencesKey("mcp_server_name")
        val KEY_MCP_SERVER_VERSION = stringPreferencesKey("mcp_server_version")
        val KEY_MCP_TOOLS_JSON = stringPreferencesKey("mcp_tools_json")

        const val DEFAULT_MCP_SERVER_URL = "http://10.0.2.2:3000/mcp"
        const val DEFAULT_MCP_ENABLED = false
    }
}
