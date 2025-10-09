package com.mycollege.schedule.app.activity.domain.usecases

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.app.activity.data.network.WebParser
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
@Singleton
class GetScheduleUseCase @Inject constructor(
    private val webParser: WebParser
) {

    @Deprecated("server migration")
    fun getSchedule(progress: (Int) -> Unit) {
        webParser.loadData(progress)
    }

}