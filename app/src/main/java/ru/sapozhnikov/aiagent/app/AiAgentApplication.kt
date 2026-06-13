package ru.sapozhnikov.aiagent.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp

/**
 * Класс приложения. Инициализирует Hilt и глобальные настройки темы.
 */
@HiltAndroidApp
class AiAgentApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
