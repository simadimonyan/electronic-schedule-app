package com.mycollege.schedule.core.cache

import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mycollege.schedule.app.activity.ui.state.AppState
import com.mycollege.schedule.feature.settings.ui.state.SettingsState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@Stable
class CacheManager @Inject constructor(
    private val preferences: SharedPreferences
) {
    private val gson = Gson()
    private val lastUpdatedKey = "last_updated_time"
    private val chosenConfigurationKey = "chosen_configuration"
    private val settingsConfKey = "settings_configuration"
    private val firstStartUp = "first_startup"
    private val studentModeKey = "student_mode"
    private val alarmsKey = "alarms"
    private val scheduleServerConfiguration = "server_config"
    private val serverNetworkLastRequest = "server_last_request"
    private val dismissedNotificationsKey = "dismissed_notifications"
    private val darkThemeKey = "dark_theme_key"

    @Immutable
    data class Configuration(
        val course: String,
        val speciality: String,
        val group: String,
        val department: String,
        val teacher: String
    )

    @Immutable
    data class IntentConf(val id: Int, val intent: Intent)

    @Immutable
    data class ScheduleServerConfiguration(val serverUrl: String, val accessToken: String)

    data class ServerNetworkLastRequest(
        val weekParitySynchronization: Long = -1,
        val groupChooseConfiguration: Long = -1,
        val teacherChooseConfiguration: Long = -1,
        val groupScheduleSynchronization: MutableMap<String, Long> = mutableMapOf<String, Long>(),
        val teacherScheduleSynchronization: MutableMap<String, Long> = mutableMapOf<String, Long>(),
    )

    private fun getServerNetworkLastRequestFlow(): Flow<ServerNetworkLastRequest> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == serverNetworkLastRequest) {
                val result = runCatching {
                    loadServerNetworkLastRequest()
                }.getOrDefault(ServerNetworkLastRequest())

                val safeResult = result ?: ServerNetworkLastRequest()
                trySend(safeResult)
            }
        }

        preferences.registerOnSharedPreferenceChangeListener(listener)

        val initialValue = runCatching {
            loadServerNetworkLastRequest()
        }.getOrDefault(ServerNetworkLastRequest())

        val safeInitialValue = initialValue ?: ServerNetworkLastRequest()
        trySend(safeInitialValue)

        awaitClose {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    fun getGroupScheduleSyncFlow(): Flow<Map<String, Long>> =
        getServerNetworkLastRequestFlow()
            .map { it.groupScheduleSynchronization ?: emptyMap() }
            .distinctUntilChanged()

    fun getTeacherScheduleSyncFlow(): Flow<Map<String, Long>> =
        getServerNetworkLastRequestFlow()
            .map { it.teacherScheduleSynchronization ?: emptyMap() }
            .distinctUntilChanged()


    fun loadServerNetworkLastRequest(): ServerNetworkLastRequest {
        val json = preferences.getString(serverNetworkLastRequest, null)

        val type = object : TypeToken<ServerNetworkLastRequest>() {}.type

        val value = try {
            gson.fromJson(json, type)
        }
        catch (e: Exception) {
            ServerNetworkLastRequest()
        }

        return value
    }

    fun saveServerNetworkLastRequest(request: ServerNetworkLastRequest) {
        val json = gson.toJson(request)
        preferences.edit() { putString(serverNetworkLastRequest, json) }
    }

    fun loadScheduleServerConfiguration(): ScheduleServerConfiguration {
        val json = preferences.getString(scheduleServerConfiguration, null)

        val type = object : TypeToken<ScheduleServerConfiguration>() {}.type

        val value = try {
            gson.fromJson(json, type)
        }
        catch (e: Exception) {
            ScheduleServerConfiguration("", "")
        }

        return value
    }

    fun saveScheduleServerConfiguration(configuration: ScheduleServerConfiguration) {
        val json = gson.toJson(configuration)
        preferences.edit() { putString(scheduleServerConfiguration, json) }
    }

    fun loadStudentMode(): Boolean {
        return preferences.getBoolean(studentModeKey, true)
    }

    fun saveStudentMode(studentMode: Boolean) {
        preferences.edit { putBoolean(studentModeKey, studentMode) }
    }

    fun saveDismissedNotification(date: String, lesson: String) {
        val dismissed = loadDismissedNotifications().toMutableSet()
        val notificationKey = "$date-$lesson"
        dismissed.add(notificationKey)
        val json = gson.toJson(dismissed)
        preferences.edit { putString(dismissedNotificationsKey, json) }
        Log.d("CacheManager", "Сохранено смахнутое уведомление: $notificationKey")
    }

    fun loadDismissedNotifications(): Set<String> {
        val json = preferences.getString(dismissedNotificationsKey, null)
        val type = object : TypeToken<Set<String>>() {}.type
        val result = try {
            gson.fromJson(json, type) ?: mutableSetOf<String>()
        } catch (e: Exception) {
            Log.e("CacheManager", "Ошибка загрузки смахнутых уведомлений", e)
            mutableSetOf<String>()
        }
        Log.d("CacheManager", "Загружены смахнутые уведомления: $result")
        return result
    }

    fun isNotificationDismissed(date: String, lesson: String): Boolean {
        val notificationKey = "$date-$lesson"
        val isDismissed = loadDismissedNotifications().contains(notificationKey)
        Log.d("CacheManager", "Проверка уведомления $notificationKey: смахнуто=$isDismissed")
        return isDismissed
    }

    fun clearDismissedNotifications() {
        preferences.edit { remove(dismissedNotificationsKey) }
        Log.d("CacheManager", "Список смахнутых уведомлений очищен")
    }

    fun loadAlarms(): ArrayList<IntentConf> {
        val json = preferences.getString(alarmsKey, null)

        val type = object : TypeToken<ArrayList<IntentConf>>() {}.type

        val value = try {
            gson.fromJson(json, type)
        }
        catch (e: Exception) {
            ArrayList<IntentConf>()
        }

        return value
    }

    fun saveAlarms(configuration: ArrayList<IntentConf>) {
        val json = gson.toJson(configuration)
        preferences.edit() { putString(alarmsKey, json) }
    }

    fun isFirstStartup(): Boolean {
        return preferences.getBoolean(firstStartUp, true)
    }

    fun setFirstStartup(isFirst: Boolean) {
        preferences.edit() { putBoolean(firstStartUp, isFirst) }
    }

    fun loadLastSettings(): SettingsState {
        val json = preferences.getString(settingsConfKey, null)

        val type = object : TypeToken<SettingsState>() {}.type

        val value = try {
            gson.fromJson(json, type)
        }
        catch (e: Exception) {
            SettingsState(false, true, false, false)
        }

        return value
    }

    fun saveActualSettings(configuration: SettingsState) {
        val json = gson.toJson(configuration)
        preferences.edit() { putString(settingsConfKey, json) }
    }

    fun loadLastConfiguration(): Configuration {
        val json = preferences.getString(chosenConfigurationKey, null)

        val type = object : TypeToken<Configuration>() {}.type

        val value = try {
            gson.fromJson(json, type)
        }
        catch (e: Exception) {
            Configuration("", "", "", "", "")
        }

        return value
    }

    fun saveActualConfiguration(configuration: Configuration) {
        val json = gson.toJson(configuration)
        preferences.edit() { putString(chosenConfigurationKey, json) }
    }

    fun getLastUpdatedTime(): Long {
        return preferences.getLong(lastUpdatedKey, 0L)
    }

    fun saveLastUpdatedTime(time: Long) {
        preferences.edit() { putLong(lastUpdatedKey, time) }
    }

    fun shouldUpdateCache(): Boolean {
        val last = getLastUpdatedTime()
        return last == 0L
    }

    fun saveAppTheme(darkTheme: Boolean) {
        preferences.edit { putBoolean(darkThemeKey, darkTheme) }
    }

    fun loadAppTheme(): Boolean {
        return preferences.getBoolean(darkThemeKey, false)
    }

}