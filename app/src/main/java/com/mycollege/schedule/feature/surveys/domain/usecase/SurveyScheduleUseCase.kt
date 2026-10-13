package com.mycollege.schedule.feature.surveys.domain.usecase

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.BuildConfig
import com.mycollege.schedule.feature.surveys.data.repository.SurveyPreferences
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Лимиты автопоказа: проверка не чаще раза в сутки, после «Не сейчас» — пауза 3 дня.
 * В debug с surveys.debugForceShow=true лимиты игнорируются.
 */
@Singleton
@Immutable
class SurveyScheduleUseCase @Inject constructor(
    private val preferences: SurveyPreferences
) {

    private val checkInterval = TimeUnit.DAYS.toMillis(1)
    private val snoozeInterval = TimeUnit.DAYS.toMillis(3)

    val forceShow: Boolean get() = BuildConfig.DEBUG && BuildConfig.SURVEYS_DEBUG_FORCE_SHOW

    fun shouldAutoCheck(now: Long = System.currentTimeMillis()): Boolean {
        if (forceShow) return true
        val snoozedAt = preferences.snoozedAt()
        if (snoozedAt > 0 && now - snoozedAt in 0 until snoozeInterval) return false
        val lastCheck = preferences.lastCheckTime()
        return lastCheck <= 0 || now - lastCheck !in 0 until checkInterval
    }

    fun markChecked(now: Long = System.currentTimeMillis()) {
        preferences.saveLastCheckTime(now)
    }

    fun snooze(now: Long = System.currentTimeMillis()) {
        preferences.saveSnoozedAt(now)
    }

}
