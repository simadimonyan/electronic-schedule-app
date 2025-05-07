package com.mycollege.schedule.feature.schedule.domain.usecase

import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import com.mycollege.schedule.feature.schedule.data.repository.WebParser

class GetScheduleUseCase {

    companion object {

        fun getSchedule(progress: (Int) -> Unit): HashMap<String, HashMap<String, ArrayList<DataClasses.Group>>> {
            return WebParser.Companion.loadData(progress)
        }

    }

}