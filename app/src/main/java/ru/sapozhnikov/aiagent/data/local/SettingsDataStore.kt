package ru.sapozhnikov.aiagent.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
)

@Singleton
internal class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val contextManagementEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_CONTEXT_MANAGEMENT_ENABLED] ?: false
    }

    suspend fun setContextManagementEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_CONTEXT_MANAGEMENT_ENABLED] = enabled
        }
    }

    suspend fun isContextManagementEnabled(): Boolean {
        return context.settingsDataStore.data
            .map { preferences -> preferences[KEY_CONTEXT_MANAGEMENT_ENABLED] ?: false }
            .first()
    }

    private companion object {
        val KEY_CONTEXT_MANAGEMENT_ENABLED = booleanPreferencesKey("context_management_enabled")
    }
}
