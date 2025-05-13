package com.mycollege.schedule.core.cache

import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import androidx.core.content.edit
import com.mycollege.schedule.feature.settings.ui.state.SettingsState

@Stable
class CacheManager @Inject constructor(
    private val preferences: SharedPreferences
) {
    private val gson = Gson()
    private val lastUpdatedKey = "last_updated_time"
    private val chosenConfigurationKey = "chosen_configuration"
    private val settingsConfKey = "settings_configuration"
    private val firstStartUp = "first_startup"
    private val alarmsKey = "alarms"
    private val rustoreConfigKey = "rustore_config"

    @Immutable
    data class Configuration(val course: String, val speciality: String, val group: String)

    @Immutable
    data class IntentConf(val id: Int, val intent: Intent)

    @Immutable
    data class RuStoreConfig(val pushToken: String, val sentToServer: Boolean)

    fun loadLastRuStoreConfig(): RuStoreConfig? {
        val json = preferences.getString(rustoreConfigKey, null)

        val type = object : TypeToken<RuStoreConfig>() {}.type

        val value = try {
            gson.fromJson(json, type)
        }
        catch (e: Exception) {
            RuStoreConfig("", false)
        }

        return value
    }

    fun saveActualRuStoreConfig(configuration: RuStoreConfig) {
        val json = gson.toJson(configuration)
        preferences.edit() { putString(rustoreConfigKey, json) }
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
            SettingsState(false, false, false)
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
            Configuration("", "", "")
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

}