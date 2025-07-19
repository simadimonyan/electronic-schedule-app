package com.mycollege.schedule.feature.groups.ui.state

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycollege.schedule.R
import com.mycollege.schedule.app.activity.domain.models.GroupParserStateHolder
import com.mycollege.schedule.app.activity.ui.state.AppStateHolder
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.core.cache.CacheUpdater
import com.mycollege.schedule.feature.groups.domain.usecases.GetCoursesUseCase
import com.mycollege.schedule.feature.groups.domain.usecases.GetGroupsUseCase
import com.mycollege.schedule.feature.groups.domain.usecases.GetLevelUseCase
import com.mycollege.schedule.shared.resources.ResourceManager
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val getGroupsUseCase: GetGroupsUseCase

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
     * Отображение состояние выбора данных группы
     */
    private fun display() {
        viewModelScope.launch {
            if (!appStateHolder.appState.value.firstStartUp) {
                val groupState = groupStateHolder.groupState.value

                val courses = getCoursesUseCase.getCourses()
                val levels = getLevelUseCase.getLevels(groupState.course)
                val groups = getGroupsUseCase.getGroups(groupState.course, groupState.level)

                groupStateHolder.updateCoursesToDisplay(courses.toList())
                groupStateHolder.updateLevelsToDisplay(levels.toList())
                groupStateHolder.updateGroupsToDisplay(groups.toList())
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
            val configuration = cacheManager.loadLastConfiguration()
            if (configuration.group.isNotEmpty()) {
                groupStateHolder.updateGroup(configuration.group)
                groupStateHolder.updateLevel(configuration.speciality)
                groupStateHolder.updateCourse(configuration.course)
            }
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
            groupStateHolder.groupState.value.group
        )
        cacheManager.saveActualConfiguration(configuration)
    }

}