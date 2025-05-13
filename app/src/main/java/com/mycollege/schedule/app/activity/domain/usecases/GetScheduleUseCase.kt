package com.mycollege.schedule.app.activity.domain.usecases

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.app.activity.data.network.GroupParser
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
@Singleton
class GetScheduleUseCase @Inject constructor(
    private val groupParser: GroupParser
) {

    fun getSchedule(progress: (Int) -> Unit) {
        groupParser.loadData(progress)
    }

}