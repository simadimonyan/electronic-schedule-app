package com.mycollege.schedule.feature.groups.ui.state

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycollege.schedule.R
import com.mycollege.schedule.app.activity.domain.models.GroupParserStateHolder
import com.mycollege.schedule.app.activity.ui.state.AppStateHolder
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.core.cache.CacheUpdater
import com.mycollege.schedule.feature.groups.domain.usecases.student.GetCoursesUseCase
import com.mycollege.schedule.feature.groups.domain.usecases.student.GetGroupsUseCase
import com.mycollege.schedule.feature.groups.domain.usecases.student.GetLevelUseCase
import com.mycollege.schedule.feature.groups.domain.usecases.teacher.GetDepartmentsUseCase
import com.mycollege.schedule.feature.groups.domain.usecases.teacher.GetTeachersUseCase
import com.mycollege.schedule.shared.resources.ResourceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class GroupViewModel @Inject constructor(

    // cache & resources
    private val resources: ResourceManager,
    private val cacheManager: CacheManager,
    private val cacheUpdater: CacheUpdater,

    // state
    val appStateHolder: AppStateHolder,
    val groupStateHolder: GroupStateHolder,
    val groupParserStateHolder: GroupParserStateHolder,

    // use cases
    private val getCoursesUseCase: GetCoursesUseCase,
    private val getLevelUseCase: GetLevelUseCase,
    private val getGroupsUseCase: GetGroupsUseCase,
    private val getTeachersUseCase: GetTeachersUseCase,
    private val getDepartmentsUseCase: GetDepartmentsUseCase

) : ViewModel() {

    fun handleEvent(event: GroupEvent) {
        when (event) {
            is GroupEvent.UpdateCourse -> updateCourse(event.course)
            is GroupEvent.UpdateSpeciality -> updateLevel(event.speciality)
            is GroupEvent.UpdateGroup -> updateGroup(event.group)
            is GroupEvent.ShowBottomSheet -> toggleBottomSheet(true)
            is GroupEvent.HideBottomSheet -> toggleBottomSheet(false)
            is GroupEvent.SetSelectedIndex -> setSelectedIndex(event.index)
            is GroupEvent.ChooseGroup -> chooseGroup()
            is GroupEvent.Display -> display()
            is GroupEvent.ChangeStudentMode -> changeAppModeToggle(event.studentMode)
            is GroupEvent.UpdateDepartment -> updateDepartment(event.department)
            is GroupEvent.UpdateTeacher -> updateTeacher(event.teacher)
        }
    }

    /**
     * Обновить кеш конфигурации состояний выбора с последнего запуска
     */
    fun init() {
        // get last chosen configuration
        viewModelScope.launch {
            restoreCache()
        }
    }

    /**
     * Изменить состояние выбора режима студента/преподавателя
     */
    private fun changeAppModeToggle(studentMode: Boolean) {
        viewModelScope.launch {
            appStateHolder.updateStudentMode(studentMode)
            cacheManager.saveStudentMode(studentMode)
        }
    }

    /**
     * Отображение состояние выбора данных группы
     */
    private fun display() {
        viewModelScope.launch {
            if (!appStateHolder.appState.value.firstStartUp) {

                val groupState = groupStateHolder.groupState.value

                if (appStateHolder.appState.value.studentMode) {
                    val courses = getCoursesUseCase.getCourses()
                    val levels = getLevelUseCase.getLevels(groupState.course)
                    val groups = getGroupsUseCase.getGroups(groupState.course, groupState.level)

                    groupStateHolder.updateCoursesToDisplay(courses.toList())
                    groupStateHolder.updateLevelsToDisplay(levels.toList())
                    groupStateHolder.updateGroupsToDisplay(groups.toList())
                }
                else {
                    val departments = getDepartmentsUseCase.getDepartments()
                    val teachers = getTeachersUseCase.getTeachers(groupState.department)

                    groupStateHolder.updateDepartmentToDisplay(departments.toList())
                    groupStateHolder.updateTeachersToDisplay(teachers.toList())
                }
            }
        }
    }

    /**
     * Обновить состояние выбора курса
     */
    private fun updateCourse(course: String) {
        viewModelScope.launch {
            groupStateHolder.updateCourse(course)
            updateLevel(resources.getString(R.string.all_specialities))
            updateGroup(resources.getString(R.string.choose))
        }
    }

    /**
     * Обновить состояние выбора уровня образования
     */
    private fun updateLevel(level: String) {
        viewModelScope.launch {
            groupStateHolder.updateLevel(level)
            updateGroup(resources.getString(R.string.choose))
        }
    }

    /**
     * Обновить состояние выбора группы
     */
    private fun updateGroup(group: String) {
        viewModelScope.launch {
            groupStateHolder.updateGroup(group)
        }
    }

    /**
     * Обновить состояние выбора кафедры
     */
    private fun updateDepartment(department: String) {
        viewModelScope.launch {
            groupStateHolder.updateDepartment(department)
            groupStateHolder.updateTeacher("Выбрать преподавателя")
        }
    }

    /**
     * Обновить состояние выбора преподавателя
     */
    private fun updateTeacher(teacher: String) {
        viewModelScope.launch {
            groupStateHolder.updateTeacher(teacher)
        }
    }

    /**
     * Обновить состояние Pager навигации по индексу
     */
    private fun setSelectedIndex(index: Int) {
        viewModelScope.launch {
            groupStateHolder.updateSelectedIndex(index)
        }
    }

    /**
     * Показать или убрать панель BottomSheet для поиска данных
     */
    private fun toggleBottomSheet(toggle: Boolean) {
        viewModelScope.launch {
            groupStateHolder.toggleBottomSheet(toggle)
        }
    }

    // screen variables init
    // in main thread only | to avoid delay of loading
    private fun restoreCache() {
        try {
            val applicationMode = cacheManager.loadStudentMode()
            val configuration = cacheManager.loadLastConfiguration()
            if (configuration.group.isNotEmpty()) {
                groupStateHolder.updateGroup(configuration.group)
                groupStateHolder.updateLevel(configuration.speciality)
                groupStateHolder.updateCourse(configuration.course)
                groupStateHolder.updateDepartment(configuration.department)
                groupStateHolder.updateTeacher(configuration.teacher)
            }
            appStateHolder.updateStudentMode(applicationMode)
        } catch (_: Exception) {
            // first-time setup or empty cache case
        }
    }

    /**
     * Выбрать расписание
     */
    private fun chooseGroup(): Boolean {
        if (!groupStateHolder.groupState.value.group.contains("Выбрать")) {
            val context = resources.getContext()

            // save configuration on schedule create only
            updateCache()

            // send signal to create schedule
            viewModelScope.launch {

                // work-manager lessons schedule
                cacheUpdater.setupPeriodicScheduleWork(context)
                groupStateHolder.sendCreateScheduleSignal()
            }
            return true
        }
        return false
    }

    /**
     * Сохранить конфигурацию выбора в кеш
     */
    private fun updateCache() {
        val configuration = CacheManager.Configuration(
            groupStateHolder.groupState.value.course,
            groupStateHolder.groupState.value.level,
            groupStateHolder.groupState.value.group,
            groupStateHolder.groupState.value.department,
            groupStateHolder.groupState.value.teacher
        )
        cacheManager.saveActualConfiguration(configuration)
    }

}