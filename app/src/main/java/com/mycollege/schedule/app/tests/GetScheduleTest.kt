package com.mycollege.schedule.app.tests

import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import com.mycollege.schedule.feature.schedule.domain.usecase.GetScheduleUseCase.Companion.getSchedule
import org.junit.jupiter.api.Test

@Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
class GetScheduleTest {

    @Test
    fun loadData() {
        val groups: HashMap<String, HashMap<String, ArrayList<DataClasses.Group>>>
        var gap: Int
        groups = getSchedule { newProgress ->
            gap = newProgress // Update progress
        }
        println(groups)
    }
}