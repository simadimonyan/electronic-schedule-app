package com.mycollege.schedule.feature.settings.ui.state

/**
 * Sealed class for managing Settings events
 */
sealed class SettingsEvent {

    // switch settings
    data class MakeScheduleWeekFull(val isFull: Boolean) : SettingsEvent()
    data class MakeNavigationInvisible(val isVisible: Boolean) : SettingsEvent()
    data class MakeWeekCountDifferent(val toChange: Boolean) : SettingsEvent()

    // save in cache
    object SaveSettings : SettingsEvent()

}