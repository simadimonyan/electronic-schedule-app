package com.mycollege.schedule.feature.surveys.ui.state

sealed class SurveyEvent {

    /** открыт экран расписания — проверить опрос с учётом лимитов */
    data object AutoCheck : SurveyEvent()

    /** «Оставить отзыв» в настройках — открыть текущий опрос без лимитов */
    data object OpenManually : SurveyEvent()

    data class SelectSingle(val questionId: Long, val answerId: Long) : SurveyEvent()

    data class ToggleMultiple(val questionId: Long, val answerId: Long) : SurveyEvent()

    data class ChangeValue(val questionId: Long, val value: String) : SurveyEvent()

    data object Submit : SurveyEvent()

    data object Retry : SurveyEvent()

    /** «Не сейчас» или смахивание листа */
    data object NotNow : SurveyEvent()

    /** закрыть лист после успеха / пустого / ошибки */
    data object Close : SurveyEvent()

}
