package com.mycollege.schedule.feature.groups.ui.state

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycollege.schedule.R
import com.mycollege.schedule.app.activity.domain.models.LoadingStateHolder
import com.mycollege.schedule.app.activity.ui.state.AppStateHolder
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.core.cache.CacheUpdater
import com.mycollege.schedule.feature.groups.domain.usecases.student.GetCoursesUseCase
import com.mycollege.schedule.feature.groups.domain.usecases.student.GetGroupScheduleUseCase
import com.mycollege.schedule.feature.groups.domain.usecases.student.GetGroupsUseCase
import com.mycollege.schedule.feature.groups.domain.usecases.student.GetLevelUseCase
import com.mycollege.schedule.feature.groups.domain.usecases.teacher.GetDepartmentsUseCase
import com.mycollege.schedule.feature.groups.domain.usecases.teacher.GetTeacherScheduleUseCase
import com.mycollege.schedule.feature.groups.domain.usecases.teacher.GetTeachersUseCase
import com.mycollege.schedule.feature.schedule.ui.state.ScheduleStateHolder
import com.mycollege.schedule.shared.resources.ResourceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Stable
@HiltViewModel
class GroupViewModel @Inject constructor(

    // cache & resources
    private val resources: ResourceManager,
    val cacheManager: CacheManager,
    private val cacheUpdater: CacheUpdater,

    // state
    val appStateHolder: AppStateHolder,
    val groupStateHolder: GroupStateHolder,
    val groupParserStateHolder: LoadingStateHolder,
    val scheduleStateHolder: ScheduleStateHolder,
    val loadingStateHolder: LoadingStateHolder,

    // use cases
    private val getCoursesUseCase: GetCoursesUseCase,
    private val getLevelUseCase: GetLevelUseCase,
    private val getGroupsUseCase: GetGroupsUseCase,
    private val getTeachersUseCase: GetTeachersUseCase,
    private val getDepartmentsUseCase: GetDepartmentsUseCase,
    private val getGroupScheduleUseCase: GetGroupScheduleUseCase,
    private val getTeacherScheduleUseCase: GetTeacherScheduleUseCase

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
            loadingStateHolder.updateScheduleLoading(false)
        }
    }

    /**
     * Отображение и подгрузка состояние выбора данных группы
     */
    private fun display() {
        viewModelScope.launch {

            val groupState = groupStateHolder.groupState.value

            try {

                val lastRequest = cacheManager.loadServerNetworkLastRequest()

                // если приложение уже запускалось ранее
                if (lastRequest != null) {

                    // запрос на сервер
                    if (appStateHolder.appState.value.studentMode) {

                        // если конфигурация не обновлялась в течение дня
                        if ((System.currentTimeMillis() - lastRequest.groupChooseConfiguration) >= TimeUnit.DAYS.toMillis(1)) {

                            Log.i("GroupViewModel", "Отправляем запрос на получение конфигурации групп")

                            loadingStateHolder.updateChooseConfigurationLoading(true)

                            val courses = getCoursesUseCase.getServerCourses()
                            val levels = getLevelUseCase.getServerLevels(groupState.course.split(" ")[0])
                            val groups = getGroupsUseCase.getServerGroups(groupState.course.split(" ")[0], courses.max()) {
                                loadingStateHolder.updateChooseConfigurationProgress(it)
                            }

                            groupStateHolder.updateCoursesToDisplay(courses.toList())
                            groupStateHolder.updateLevelsToDisplay(levels.toList())
                            groupStateHolder.updateGroupsToDisplay(groups.toList())

                            loadingStateHolder.updateChooseConfigurationLoading(false)
                            loadingStateHolder.updateChooseConfigurationProgress(0)

                        }
                        else { // если обновлялось в течение дня то берем из бд

                            Log.i("GroupViewModel", "Debounce конфигурации групп: частота запроса выше порога - 1 день")

                            val courses = getCoursesUseCase.getRoomCourses()
                            val levels = getLevelUseCase.getRoomLevels(groupState.course.split(" ")[0])
                            val groups = getGroupsUseCase.getRoomGroups(groupState.course.split(" ")[0], groupState.level)

                            groupStateHolder.updateCoursesToDisplay(courses.toList())
                            groupStateHolder.updateLevelsToDisplay(levels.toList())
                            groupStateHolder.updateGroupsToDisplay(groups.toList())

                        }

                    }
                    else {

                        // если конфигурация не обновлялась в течение дня
                        if ((System.currentTimeMillis() - lastRequest.teacherChooseConfiguration) >= TimeUnit.DAYS.toMillis(1)) {

                            Log.i("GroupViewModel", "Отправляем запрос на получение конфигурации преподавателей")

                            loadingStateHolder.updateChooseConfigurationLoading(true)

                            val departments = getDepartmentsUseCase.getRoomDepartments()
                            val teachers = getTeachersUseCase.getServerTeachers {
                                loadingStateHolder.updateChooseConfigurationProgress(it)
                            }

                            groupStateHolder.updateDepartmentToDisplay(departments.toList())
                            groupStateHolder.updateTeachersToDisplay(teachers.toList())

                            loadingStateHolder.updateChooseConfigurationLoading(false)
                            loadingStateHolder.updateChooseConfigurationProgress(0)

                        }
                        else { // если обновлялось в течение дня то берем из бд

                            Log.i("GroupViewModel", "Debounce конфигурации преподавателей: частота запроса выше порога - 1 день")

                            val departments = getDepartmentsUseCase.getRoomDepartments()
                            val teachers = getTeachersUseCase.getRoomTeachers(groupState.department)

                            groupStateHolder.updateDepartmentToDisplay(departments.toList())
                            groupStateHolder.updateTeachersToDisplay(teachers.toList())

                        }

                    }

                }
                else { // делаем запрос на сервер при первом запуске

                    // запрос на сервер
                    if (appStateHolder.appState.value.studentMode) {

                        Log.i("GroupViewModel", "Первый запрос конфигурации групп")

                        loadingStateHolder.updateChooseConfigurationLoading(true)

                        val courses = getCoursesUseCase.getServerCourses()
                        val levels = getLevelUseCase.getServerLevels(groupState.course.split(" ")[0])
                        val groups = getGroupsUseCase.getServerGroups(groupState.course.split(" ")[0], courses.max()) {
                            loadingStateHolder.updateChooseConfigurationProgress(it)
                        }

                        groupStateHolder.updateCoursesToDisplay(courses.toList())
                        groupStateHolder.updateLevelsToDisplay(levels.toList())
                        groupStateHolder.updateGroupsToDisplay(groups.toList())

                        loadingStateHolder.updateChooseConfigurationLoading(false)
                        loadingStateHolder.updateChooseConfigurationProgress(0)

                    }
                    else {

                        Log.i("GroupViewModel", "Первый запрос конфигурации преподавателей")

                        loadingStateHolder.updateChooseConfigurationLoading(true)

                        val departments = getDepartmentsUseCase.getRoomDepartments()
                        val teachers = getTeachersUseCase.getServerTeachers {
                            loadingStateHolder.updateChooseConfigurationProgress(it)
                        }

                        groupStateHolder.updateDepartmentToDisplay(departments.toList())
                        groupStateHolder.updateTeachersToDisplay(teachers.toList())

                        loadingStateHolder.updateChooseConfigurationLoading(false)
                        loadingStateHolder.updateChooseConfigurationProgress(0)

                    }

                }

            }
            catch (e: Exception) {

                // в том числе проблемы с сетью
                Log.e("GroupViewModel", "Ошибка при получении данных - $e")

                if (appStateHolder.appState.value.studentMode) {

                    val courses = getCoursesUseCase.getRoomCourses()
                    val levels = getLevelUseCase.getRoomLevels(groupState.course.split(" ")[0])
                    val groups = getGroupsUseCase.getRoomGroups(groupState.course.split(" ")[0], groupState.level)

                    // берем данные из базы
                    if (courses.isNotEmpty()) {
                        groupStateHolder.updateCoursesToDisplay(courses.toList())
                        groupStateHolder.updateLevelsToDisplay(levels.toList())
                        groupStateHolder.updateGroupsToDisplay(groups.toList())
                    }
                    else {// если в базе нет данных - бесконечная загрузка
                        loadingStateHolder.updateChooseConfigurationLoading(true)
                        loadingStateHolder.updateChooseConfigurationProgress(10)
                    }

                }
                else {

                    val departments = getDepartmentsUseCase.getRoomDepartments()
                    val teachers = getTeachersUseCase.getRoomTeachers(groupState.department)

                    // берем данные из базы
                    if (teachers.isNotEmpty()) {
                        groupStateHolder.updateDepartmentToDisplay(departments.toList())
                        groupStateHolder.updateTeachersToDisplay(teachers.toList())
                    }
                    else {// если в базе нет данных - бесконечная загрузка
                        loadingStateHolder.updateChooseConfigurationLoading(true)
                        loadingStateHolder.updateChooseConfigurationProgress(10)
                    }
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
            scheduleStateHolder.updateBuildScheduleGroupModeFlag(false)
            loadingStateHolder.updateScheduleLoading(false)
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
            scheduleStateHolder.updateBuildScheduleTeacherModeFlag(false)
            loadingStateHolder.updateScheduleLoading(false)
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

                try {

                    val lastRequest = cacheManager.loadServerNetworkLastRequest()

                    if (lastRequest != null) {

                        if (appStateHolder.appState.value.studentMode) {

                            if (lastRequest.groupScheduleSynchronization.isEmpty()
                                || lastRequest.groupScheduleSynchronization[groupStateHolder.groupState.value.group] == null
                                || System.currentTimeMillis() - lastRequest.groupScheduleSynchronization[groupStateHolder.groupState.value.group]!!.toLong()
                                    >= TimeUnit.MINUTES.toMillis(10)
                            ) {
                                loadingStateHolder.updateScheduleLoading(true)

                                Log.i("GroupViewModel", "Отправляем запрос на получение расписания группы")

                                getGroupScheduleUseCase.getServerGroupSchedule(groupStateHolder.groupState.value.group)
                                cacheUpdater.setupPeriodicScheduleWork(context)

                                loadingStateHolder.updateScheduleLoading(false)
                            }
                            else
                                Log.i("GroupViewModel", "Debounce подгрузки расписания группы: частота запроса выше порога - 10 минут")

                            // сигнал для сборки расписания
                            scheduleStateHolder.updateBuildScheduleGroupModeFlag(true)
                        }
                        else {

                            if (lastRequest.teacherScheduleSynchronization.isEmpty()
                                || lastRequest.teacherScheduleSynchronization[groupStateHolder.groupState.value.teacher] == null
                                || System.currentTimeMillis() - lastRequest.teacherScheduleSynchronization[groupStateHolder.groupState.value.teacher]!!.toLong()
                                >= TimeUnit.MINUTES.toMillis(10)
                            ) {
                                loadingStateHolder.updateScheduleLoading(true)

                                Log.i("GroupViewModel", "Отправляем запрос на получение расписания преподавателя")

                                getTeacherScheduleUseCase.getServerTeacherSchedule(groupStateHolder.groupState.value.teacher)
                                cacheUpdater.setupPeriodicScheduleWork(context)

                                loadingStateHolder.updateScheduleLoading(false)
                            }
                            else
                                Log.i("GroupViewModel", "Debounce подгрузки расписания преподавателя: частота запроса выше порога - 10 минут")

                            // сигнал для сборки расписания
                            scheduleStateHolder.updateBuildScheduleTeacherModeFlag(true)
                        }

                    }
                    else {

                        Log.i("GroupViewModel", "Первый запрос расписания")

                        // начало процесса загрузки
                        loadingStateHolder.updateScheduleLoading(true)

                        // отправить запрос на загрузку расписания
                        if (appStateHolder.appState.value.studentMode) {
                            getGroupScheduleUseCase.getServerGroupSchedule(groupStateHolder.groupState.value.group)
                        }
                        else
                            getTeacherScheduleUseCase.getServerTeacherSchedule(groupStateHolder.groupState.value.teacher)

                        // work-manager lessons schedule
                        cacheUpdater.setupPeriodicScheduleWork(context)

                        // отправка сигнала для сборки расписания
                        loadingStateHolder.updateScheduleLoading(false)
                        //groupStateHolder.sendCreateScheduleSignal()
                        if (appStateHolder.appState.value.studentMode) scheduleStateHolder.updateBuildScheduleGroupModeFlag(true)
                        else scheduleStateHolder.updateBuildScheduleTeacherModeFlag(true)

                    }

                }
                catch (e: Exception) { // проблемы с интернетом
                    Log.e("GroupViewModel", "Сборка расписания: $e")

                    val lastRequest = cacheManager.loadServerNetworkLastRequest()

                    if (lastRequest != null) {

                        if (appStateHolder.appState.value.studentMode) {

                            // если при отсутствии интернета есть кеш группы - собираем расписание
                            if (lastRequest.groupScheduleSynchronization.isNotEmpty()
                                && lastRequest.groupScheduleSynchronization[groupStateHolder.groupState.value.group] != null) {
                                Log.i("GroupViewModel", "Проблемы с интернетом - собираем расписание группы из кеша")
                                loadingStateHolder.updateScheduleLoading(false)
                                scheduleStateHolder.updateBuildScheduleGroupModeFlag(true)
                            }

                        }
                        else {

                            // если при отсутствии интернета есть кеш преподавателя - собираем расписание
                            if (lastRequest.teacherScheduleSynchronization.isNotEmpty()
                                && lastRequest.teacherScheduleSynchronization[groupStateHolder.groupState.value.teacher] != null) {
                                Log.i("GroupViewModel", "Проблемы с интернетом - собираем расписание преподавателя из кеша")
                                loadingStateHolder.updateScheduleLoading(false)
                                scheduleStateHolder.updateBuildScheduleTeacherModeFlag(true)
                            }

                        }

                    }
                }

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