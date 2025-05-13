package com.mycollege.schedule.feature.groups.domain.usecases

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.core.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Immutable
class GetCoursesUseCase @Inject constructor(
    private val database: Database
) {

    suspend fun getCourses(): Set<String> {
        return withContext(Dispatchers.IO) {
            return@withContext database.groups().getCourses().toSortedSet()
        }
    }

}