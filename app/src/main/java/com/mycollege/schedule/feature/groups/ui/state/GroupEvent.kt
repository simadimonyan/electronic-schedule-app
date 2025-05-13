package com.mycollege.schedule.feature.groups.ui.state

/**
 * Sealed class for managing Group Screen events
 */
sealed class GroupEvent {

    // updating chosen Group Screen configuration fields
    data class UpdateCourse(val course: String) : GroupEvent()
    data class UpdateSpeciality(val speciality: String) : GroupEvent()
    data class UpdateGroup(val group: String) : GroupEvent()

    // interaction
    object ShowBottomSheet : GroupEvent()
    object HideBottomSheet : GroupEvent()

    // chosen configuration field index
    data class SetSelectedIndex(val index: Int) : GroupEvent()

    // action button event
    object ChooseGroup : GroupEvent()

    // display data for group search
    object Display : GroupEvent()

}