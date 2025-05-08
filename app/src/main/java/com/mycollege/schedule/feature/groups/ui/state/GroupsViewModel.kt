package com.mycollege.schedule.feature.groups.ui.state

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycollege.schedule.R
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.shared.resources.ResourceManager
import com.mycollege.schedule.shared.state.SharedStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val resources: ResourceManager,
    private val cacheManager: CacheManager,
    val shared: SharedStateRepository
) : ViewModel() {

    private var _groupState = MutableStateFlow(GroupState())
    val groupState: StateFlow<GroupState> = _groupState

    fun handleEvent(event: GroupEvent) {
        when (event) {
            is GroupEvent.UpdateCourse -> updateCourse(event.course)
            is GroupEvent.UpdateSpeciality -> updateSpeciality(event.speciality)
            is GroupEvent.UpdateGroup -> updateGroup(event.group)
            is GroupEvent.ShowBottomSheet -> toggleBottomSheet(true)
            is GroupEvent.HideBottomSheet -> toggleBottomSheet(false)
            is GroupEvent.SetSelectedIndex -> setSelectedIndex(event.index)
            is GroupEvent.CreateSchedule -> createSchedule()
            is GroupEvent.DisplayGroups -> displayGroups(event.course, event.speciality)
            is GroupEvent.DisplayCourses -> displayCourses()
            is GroupEvent.DisplaySpecialities -> displaySpecialities(event.course)
        }
    }

    fun init() {

        // subscribe UI state on external groups loading
        viewModelScope.launch {
            shared.groups.collect { newGroups ->
                _groupState.update { currentState ->
                    val allGroups = mutableListOf<String>()

                    newGroups[currentState.course]?.keys?.let { specialities ->
                        newGroups[currentState.course]?.forEach { (_, groups) ->

                            // all groups of all specialities by default
                            groups.forEach { group ->
                                allGroups.add(group.group)
                            }
                        }

                        // change state of groups to display by defaults when loading finished
                        currentState.copy(
                            coursesToDisplay = newGroups.keys.toList(),
                            specialitiesToDisplay = specialities.toList(),
                            groupsToDisplay = allGroups
                        )
                    } ?: currentState
                }
            }
        }

        // get last chosen configuration
        viewModelScope.launch {
            restoreCache()
        }

    }

    private fun createSchedule() {
        viewModelScope.launch {
            _groupState.update { it.copy(scheduleCreation = createScheduleState()) }
        }
    }

    private fun displayGroups(course: String, speciality: String) {
        viewModelScope.launch {
            _groupState.update { it.copy(groupsToDisplay = getGroupsToDisplay(course, speciality)) }
        }
    }

    private fun displaySpecialities(course: String) {
        viewModelScope.launch {
            _groupState.update { it.copy(specialitiesToDisplay = getSpecialitiesToDisplay(course)) }
        }
    }

    private fun displayCourses() {
        viewModelScope.launch {
            _groupState.update { it.copy(coursesToDisplay = getCoursesToDisplay()) }
        }
    }

    private fun updateCourse(course: String) {
        viewModelScope.launch {
            _groupState.update { it.copy(course = course) }
            updateSpeciality(resources.getString(R.string.all_specialities))
            updateGroup(resources.getString(R.string.choose))
        }
    }

    private fun updateSpeciality(speciality: String) {
        viewModelScope.launch {
            _groupState.update { it.copy(speciality = speciality) }
            updateGroup(resources.getString(R.string.choose))
        }
    }

    private fun updateGroup(group: String) {
        viewModelScope.launch {
            _groupState.update { it.copy(group = group) }
        }
    }

    private fun setSelectedIndex(index: Int) {
        viewModelScope.launch {
            _groupState.update { it.copy(selectedIndex = index) }
        }
    }

    private fun toggleBottomSheet(toggle: Boolean) {
        viewModelScope.launch {
            _groupState.update { it.copy(showBottomSheet = toggle) }
        }
    }

    // screen variables init
    // in main thread only | to avoid delay of loading
    private fun restoreCache() {
        try {
            val configuration = cacheManager.loadLastConfiguration()
            if (configuration.group.isNotEmpty()) {
                _groupState.update { it.copy(
                    course = configuration.course,
                    speciality = configuration.speciality,
                    group = configuration.group
                )}
            }
        } catch (e: Exception) {
            // first-time setup or empty cache case
        }
    }

    // action button
    private fun createScheduleState(): Boolean {
        if (!groupState.value.group.contains("Выбрать")) {
            shared.updateCourse(groupState.value.course)
            shared.updateSpeciality(groupState.value.speciality)
            shared.updateGroup(groupState.value.group)

            // save configuration on schedule create only
            updateCache()

            // send signal to create schedule
            viewModelScope.launch {
                shared.sendCreateScheduleSignal()
            }
            return true
        }
        return false
    }

    private fun getCoursesToDisplay(): List<String> {
        return shared.groups.value.keys.toList()
    }

    private fun getSpecialitiesToDisplay(courseChosen: String): List<String> {
        return shared.groups.value[courseChosen]?.keys?.toList().orEmpty()
    }

    private fun getGroupsToDisplay(courseChosen: String, specialityChosen: String): List<String> {
        return if (specialityChosen != "Все специальности") {
            shared.groups.value[courseChosen]?.get(specialityChosen)?.map { it.group }.orEmpty()
        } else {
            shared.groups.value[courseChosen]?.values?.flatten()?.map { it.group }.orEmpty()
        }
    }

    private fun updateCache() {
        val configuration = CacheManager.Configuration(
            _groupState.value.course,
            _groupState.value.speciality,
            _groupState.value.group
        )
        cacheManager.saveActualConfiguration(configuration)
    }

}