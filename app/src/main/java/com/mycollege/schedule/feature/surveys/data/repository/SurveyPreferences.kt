package com.mycollege.schedule.feature.surveys.data.repository

import android.content.SharedPreferences
import androidx.compose.runtime.Stable
import androidx.core.content.edit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Локальное состояние опросов: идентификатор установки и метки времени показов.
 * Хранится в тех же SharedPreferences, что и CacheManager.
 */
@Stable
@Singleton
class SurveyPreferences @Inject constructor(
    private val preferences: SharedPreferences
) {

    private val installationIdKey = "surveys_installation_id"
    private val lastCheckKey = "surveys_last_check"
    private val snoozedAtKey = "surveys_snoozed_at"

    /**
     * Идентификатор установки — генерируется один раз и живёт до переустановки приложения
     */
    @Synchronized
    fun installationId(): String {
        val saved = preferences.getString(installationIdKey, null)
        if (!saved.isNullOrBlank()) return saved

        val generated = UUID.randomUUID().toString()
        preferences.edit { putString(installationIdKey, generated) }
        return generated
    }

    /**
     * respondent по контракту: android:<uuid установки>
     */
    fun respondent(): String = "android:${installationId()}"

    fun lastCheckTime(): Long = preferences.getLong(lastCheckKey, 0L)

    fun saveLastCheckTime(time: Long) {
        preferences.edit { putLong(lastCheckKey, time) }
    }

    fun snoozedAt(): Long = preferences.getLong(snoozedAtKey, 0L)

    fun saveSnoozedAt(time: Long) {
        preferences.edit { putLong(snoozedAtKey, time) }
    }

}
