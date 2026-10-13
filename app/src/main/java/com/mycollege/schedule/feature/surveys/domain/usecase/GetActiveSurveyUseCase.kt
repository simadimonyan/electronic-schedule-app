package com.mycollege.schedule.feature.surveys.domain.usecase

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.app.activity.ui.state.AppStateHolder
import com.mycollege.schedule.core.network.dto.quizzes.Quiz
import com.mycollege.schedule.feature.groups.ui.state.GroupStateHolder
import com.mycollege.schedule.feature.schedule.domain.usecase.GetChosenGroupUseCase
import com.mycollege.schedule.feature.surveys.data.models.QuestionType
import com.mycollege.schedule.feature.surveys.data.repository.SurveyPreferences
import com.mycollege.schedule.feature.surveys.data.repository.SurveyRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Текущий опрос для этой установки (или null, если открытых опросов нет)
 */
@Singleton
@Immutable
class GetActiveSurveyUseCase @Inject constructor(
    private val repository: SurveyRepository,
    private val preferences: SurveyPreferences,
    private val appStateHolder: AppStateHolder,
    private val groupStateHolder: GroupStateHolder,
    private val getChosenGroupUseCase: GetChosenGroupUseCase
) {

    fun isConfigured(): Boolean = repository.isConfigured()

    /**
     * Группа студента — так же, как её берёт расписание: имя из GroupState, проверенное по Room.
     * У преподавателя и без выбранной группы — null (в запрос не передаётся).
     */
    suspend fun respondentGroup(): String? {
        if (!appStateHolder.appState.value.studentMode) return null
        val name = groupStateHolder.groupState.value.group
        if (name.isBlank() || name == "Выбрать группу") return null
        return getChosenGroupUseCase.getByName(name)?.name?.takeIf { it.isNotBlank() } ?: name
    }

    suspend fun getActiveSurvey(): Quiz? {
        val quizzes = repository.search(respondentGroup(), preferences.respondent())
        val quiz = quizzes.firstOrNull() ?: return null

        // search обычно отдаёт полный QuizResponse; если вопросов нет — догружаем опрос
        val full = if (quiz.questions.isNullOrEmpty()) repository.quiz(quiz.id) else quiz
        val questions = full.questions.orEmpty().filter { QuestionType.of(it) != QuestionType.UNKNOWN }

        return if (questions.isEmpty()) null else full.copy(questions = questions)
    }

}
