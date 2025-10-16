package com.mycollege.schedule.feature.groups.domain.usecases.teacher

import androidx.compose.runtime.Immutable
import com.my.tracker.MyTracker
import com.mycollege.schedule.core.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
@Singleton
class GetDepartmentsUseCase @Inject constructor(
    private val database: Database
) {

    suspend fun getRoomDepartments(): Set<String> {
        return withContext(Dispatchers.IO) {
            MyTracker.trackEvent("LocalGetDepartmentsUseCaseEvent")
            val result = database.teachers().getDepartments().toMutableSet()
            result.add("Все кафедры")
            result.toSortedSet()
        }
    }

}