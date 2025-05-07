package com.mycollege.schedule.core.cache

import android.content.Intent
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import javax.inject.Inject

class CacheManager @Inject constructor(
    private val preferences: SharedPreferences
) {
    private val gson = Gson()
    private val lastUpdatedKey = "last_updated_time"
    private val groupsCacheKey = "groups_cache"
    private val chosenConfigurationKey = "chosen_configuration"
    private val settingsConfKey = "settings_configuration"
    private val todayScheduleKey = "today_schedule_key"
    private val firstStartUp = "first_startup"
    private val alarmsKey = "alarms"
    private val rustoreConfigKey = "rustore_config"

    data class Configuration(val course: String, val speciality: String, val group: String)

    data class IntentConf(val id: Int, val intent: Intent)

    data class Settings(val fullWeek: Boolean, val isNavInvisible: Boolean, val changeWeekCount: Boolean = false)

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
        preferences.edit().putString(rustoreConfigKey, json).apply()
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
        preferences.edit().putString(alarmsKey, json).apply()
    }

    fun isFirstStartup(): Boolean {
        return preferences.getBoolean(firstStartUp, true)
    }

    fun setFirstStartup(isFirst: Boolean) {
        preferences.edit().putBoolean(firstStartUp, isFirst).apply()
    }

    fun loadTodaySchedule(): ArrayList<DataClasses.Lesson> {
        val json = preferences.getString(todayScheduleKey, null)

        val type = object : TypeToken<ArrayList<DataClasses.Lesson>>() {}.type

        val value = try {
            gson.fromJson(json, type)
        }
        catch (e: Exception) {
            ArrayList<DataClasses.Lesson>()
        }

        return value
    }

    fun saveTodaySchedule(configuration: ArrayList<DataClasses.Lesson>) {
        val json = gson.toJson(configuration)
        preferences.edit().putString(todayScheduleKey, json).apply()
    }

    fun loadLastSettings(): Settings {
        val json = preferences.getString(settingsConfKey, null)

        val type = object : TypeToken<Settings>() {}.type

        val value = try {
            gson.fromJson(json, type)
        }
        catch (e: Exception) {
            Settings(fullWeek = false, isNavInvisible = false, changeWeekCount = false)
        }

        return value
    }

    fun saveActualSettings(configuration: Settings) {
        val json = gson.toJson(configuration)
        preferences.edit().putString(settingsConfKey, json).apply()
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
        preferences.edit().putString(chosenConfigurationKey, json).apply()
    }

    fun getLastUpdatedTime(): Long {
        return preferences.getLong(lastUpdatedKey, 0L)
    }

    fun saveLastUpdatedTime(time: Long) {
        preferences.edit().putLong(lastUpdatedKey, time).apply()
    }

    fun shouldUpdateCache(): Boolean {
        val last = getLastUpdatedTime()
        return last == 0L
    }

    fun saveGroupsToCache(groups: HashMap<String, HashMap<String, ArrayList<DataClasses.Group>>>) {
        val json = gson.toJson(groups)
        preferences.edit().putString(groupsCacheKey, json).apply()
    }

    fun loadGroupsFromCache(): HashMap<String, HashMap<String, ArrayList<DataClasses.Group>>> {
        val json = preferences.getString(groupsCacheKey, null) ?: return HashMap()

        val type = object : TypeToken<HashMap<String, HashMap<String, ArrayList<DataClasses.Group>>>>() {}.type
        val groups: HashMap<String, HashMap<String, ArrayList<DataClasses.Group>>> = try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            return HashMap()
        }

        return if (groups.isNotEmpty()) {
            val sorted = groups.toSortedMap(Comparator.comparingInt {
                it.split(" ")[0].toInt()
            }).toMutableMap() as HashMap<String, HashMap<String, ArrayList<DataClasses.Group>>>

            sorted
        } else {
            HashMap()
        }
    }

}