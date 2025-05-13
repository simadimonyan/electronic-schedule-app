package com.mycollege.schedule.feature.groups.domain.usecases

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.core.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Immutable
class GetLevelUseCase @Inject constructor(
    private val database: Database
) {

    suspend fun getLevels(course: String): Set<String> {
        return withContext(Dispatchers.IO) {
            return@withContext database.groups().getLevelsBy(course).toSortedSet()
        }
    }

}