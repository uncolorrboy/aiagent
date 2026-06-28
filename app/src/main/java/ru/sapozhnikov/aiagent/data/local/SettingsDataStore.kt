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
import ru.sapozhnikov.aiagent.domain.model.McpServerConfig
import ru.sapozhnikov.aiagent.domain.model.McpToolDefinition
import java.util.UUID
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

    /** Поток списка настроенных MCP-серверов. */
    val mcpServers: Flow<List<McpServerConfig>> = context.settingsDataStore.data.map { preferences ->
        resolveMcpServers(preferences)
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

    suspend fun getMcpServers(): List<McpServerConfig> {
        return context.settingsDataStore.data
            .map { preferences -> resolveMcpServers(preferences) }
            .first()
    }

    suspend fun saveMcpServers(servers: List<McpServerConfig>) {
        val serversJson = gson.toJson(servers.map { McpServerConfigDto.fromDomain(it) })
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_MCP_SERVERS_JSON] = serversJson
            clearLegacyMcpKeys(preferences)
        }
    }

    suspend fun updateMcpServer(server: McpServerConfig) {
        val servers = getMcpServers().map { existing ->
            if (existing.id == server.id) server else existing
        }
        saveMcpServers(servers)
    }

    suspend fun addMcpServer(server: McpServerConfig) {
        saveMcpServers(getMcpServers() + server)
    }

    suspend fun removeMcpServer(serverId: String) {
        saveMcpServers(getMcpServers().filterNot { it.id == serverId })
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

    private fun resolveMcpServers(preferences: Preferences): List<McpServerConfig> {
        preferences[KEY_MCP_SERVERS_JSON]?.let { json ->
            runCatching {
                gson.fromJson(json, Array<McpServerConfigDto>::class.java)
                    ?.map { it.toDomain() }
            }.getOrNull()?.let { return it }
        }
        return migrateLegacyMcpServer(preferences)
    }

    private fun migrateLegacyMcpServer(preferences: Preferences): List<McpServerConfig> {
        val legacyUrl = preferences[KEY_MCP_SERVER_URL] ?: return emptyList()
        if (legacyUrl.isBlank()) return emptyList()

        val tools = preferences[KEY_MCP_TOOLS_JSON]?.let { json ->
            runCatching {
                gson.fromJson(json, Array<McpToolDefinitionDto>::class.java)
                    ?.map { it.toDomain() }
            }.getOrNull()
        } ?: emptyList()

        return listOf(
            McpServerConfig(
                id = UUID.randomUUID().toString(),
                url = legacyUrl,
                authToken = preferences[KEY_MCP_AUTH_TOKEN].orEmpty(),
                enabled = preferences[KEY_MCP_ENABLED] ?: DEFAULT_MCP_ENABLED,
                serverName = preferences[KEY_MCP_SERVER_NAME],
                serverVersion = preferences[KEY_MCP_SERVER_VERSION],
                tools = tools,
            ),
        )
    }

    private fun clearLegacyMcpKeys(preferences: androidx.datastore.preferences.core.MutablePreferences) {
        preferences.remove(KEY_MCP_SERVER_URL)
        preferences.remove(KEY_MCP_AUTH_TOKEN)
        preferences.remove(KEY_MCP_SERVER_NAME)
        preferences.remove(KEY_MCP_SERVER_VERSION)
        preferences.remove(KEY_MCP_TOOLS_JSON)
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

    private data class McpOAuthTokensDto(
        val accessToken: String,
        val refreshToken: String? = null,
        val expiresAtEpochSeconds: Long? = null,
        val tokenType: String? = null,
    ) {
        fun toDomain() = ru.sapozhnikov.aiagent.domain.model.McpOAuthTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAtEpochSeconds = expiresAtEpochSeconds,
            tokenType = tokenType ?: "Bearer",
        )

        companion object {
            fun fromDomain(tokens: ru.sapozhnikov.aiagent.domain.model.McpOAuthTokens) = McpOAuthTokensDto(
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken,
                expiresAtEpochSeconds = tokens.expiresAtEpochSeconds,
                tokenType = tokens.tokenType,
            )
        }
    }

    private data class McpOAuthClientInfoDto(
        val clientId: String,
        val clientSecret: String? = null,
        val issuer: String? = null,
    ) {
        fun toDomain() = ru.sapozhnikov.aiagent.domain.model.McpOAuthClientInfo(
            clientId = clientId,
            clientSecret = clientSecret,
            issuer = issuer,
        )

        companion object {
            fun fromDomain(info: ru.sapozhnikov.aiagent.domain.model.McpOAuthClientInfo) = McpOAuthClientInfoDto(
                clientId = info.clientId,
                clientSecret = info.clientSecret,
                issuer = info.issuer,
            )
        }
    }

    private data class McpOAuthDiscoveryStateDto(
        val authorizationServerUrl: String,
        val resourceMetadataUrl: String? = null,
        val authorizationEndpoint: String? = null,
        val tokenEndpoint: String? = null,
        val registrationEndpoint: String? = null,
        val issuer: String? = null,
        val scopesSupported: List<String>? = null,
    ) {
        fun toDomain() = ru.sapozhnikov.aiagent.domain.model.McpOAuthDiscoveryState(
            authorizationServerUrl = authorizationServerUrl,
            resourceMetadataUrl = resourceMetadataUrl,
            authorizationEndpoint = authorizationEndpoint,
            tokenEndpoint = tokenEndpoint,
            registrationEndpoint = registrationEndpoint,
            issuer = issuer,
            scopesSupported = scopesSupported.orEmpty(),
        )

        companion object {
            fun fromDomain(state: ru.sapozhnikov.aiagent.domain.model.McpOAuthDiscoveryState) =
                McpOAuthDiscoveryStateDto(
                    authorizationServerUrl = state.authorizationServerUrl,
                    resourceMetadataUrl = state.resourceMetadataUrl,
                    authorizationEndpoint = state.authorizationEndpoint,
                    tokenEndpoint = state.tokenEndpoint,
                    registrationEndpoint = state.registrationEndpoint,
                    issuer = state.issuer,
                    scopesSupported = state.scopesSupported,
                )
        }
    }

    private data class McpServerConfigDto(
        val id: String,
        val url: String,
        val authType: String? = null,
        val authToken: String?,
        val oauthTokens: McpOAuthTokensDto? = null,
        val oauthClientInfo: McpOAuthClientInfoDto? = null,
        val oauthDiscovery: McpOAuthDiscoveryStateDto? = null,
        val enabled: Boolean,
        val serverName: String?,
        val serverVersion: String?,
        val tools: List<McpToolDefinitionDto>?,
    ) {
        fun toDomain() = McpServerConfig(
            id = id,
            url = url,
            authType = authType?.let { runCatching { ru.sapozhnikov.aiagent.domain.model.McpAuthType.valueOf(it) }.getOrNull() }
                ?: if (!authToken.isNullOrBlank()) {
                    ru.sapozhnikov.aiagent.domain.model.McpAuthType.BEARER
                } else {
                    ru.sapozhnikov.aiagent.domain.model.McpAuthType.NONE
                },
            authToken = authToken.orEmpty(),
            oauthTokens = oauthTokens?.toDomain(),
            oauthClientInfo = oauthClientInfo?.toDomain(),
            oauthDiscovery = oauthDiscovery?.toDomain(),
            enabled = enabled,
            serverName = serverName,
            serverVersion = serverVersion,
            tools = tools?.map { it.toDomain() }.orEmpty(),
        )

        companion object {
            fun fromDomain(config: McpServerConfig) = McpServerConfigDto(
                id = config.id,
                url = config.url,
                authType = config.authType.name,
                authToken = config.authToken.takeIf { it.isNotBlank() },
                oauthTokens = config.oauthTokens?.let { McpOAuthTokensDto.fromDomain(it) },
                oauthClientInfo = config.oauthClientInfo?.let { McpOAuthClientInfoDto.fromDomain(it) },
                oauthDiscovery = config.oauthDiscovery?.let { McpOAuthDiscoveryStateDto.fromDomain(it) },
                enabled = config.enabled,
                serverName = config.serverName,
                serverVersion = config.serverVersion,
                tools = config.tools.map { McpToolDefinitionDto.fromDomain(it) },
            )
        }
    }

    private companion object {
        val KEY_CONTEXT_MANAGEMENT_STRATEGY = stringPreferencesKey("context_management_strategy")
        val KEY_CONTEXT_MANAGEMENT_ENABLED = booleanPreferencesKey("context_management_enabled")
        val KEY_MCP_ENABLED = booleanPreferencesKey("mcp_enabled")
        val KEY_MCP_SERVERS_JSON = stringPreferencesKey("mcp_servers_json")
        val KEY_MCP_SERVER_URL = stringPreferencesKey("mcp_server_url")
        val KEY_MCP_AUTH_TOKEN = stringPreferencesKey("mcp_auth_token")
        val KEY_MCP_SERVER_NAME = stringPreferencesKey("mcp_server_name")
        val KEY_MCP_SERVER_VERSION = stringPreferencesKey("mcp_server_version")
        val KEY_MCP_TOOLS_JSON = stringPreferencesKey("mcp_tools_json")

        const val DEFAULT_MCP_ENABLED = false
    }
}
