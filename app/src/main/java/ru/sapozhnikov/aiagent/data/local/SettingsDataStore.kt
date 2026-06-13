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

    /** Поток выбранной стратегии управления контекстом. */
    val contextManagementStrategy: Flow<ContextManagementStrategy> = context.settingsDataStore.data.map { preferences ->
        resolveStrategy(preferences)
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

    private companion object {
        val KEY_CONTEXT_MANAGEMENT_STRATEGY = stringPreferencesKey("context_management_strategy")
        val KEY_CONTEXT_MANAGEMENT_ENABLED = booleanPreferencesKey("context_management_enabled")
    }
}
