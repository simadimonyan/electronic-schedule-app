package com.mycollege.schedule.app.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mycollege.schedule.shared.ui.design.MainTab

/**
 * Выбранная вкладка расписания («Сегодня» / «Неделя») — общее UI-состояние
 * для экрана расписания и нижней навигации на экране настроек.
 * null — ещё не выбрана: берётся из настройки «Открывать сразу неделю».
 */
object MainTabs {
    var scheduleTab by mutableStateOf<MainTab?>(null)
}
