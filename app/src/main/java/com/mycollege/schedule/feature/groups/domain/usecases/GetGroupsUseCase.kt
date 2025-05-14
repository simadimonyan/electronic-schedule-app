package com.mycollege.schedule.feature.groups.domain.usecases

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.core.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Immutable
class GetGroupsUseCase @Inject constructor(
    private val database: Database
) {

    suspend fun getGroups(course: String, level: String): Set<String> {
        return withContext(Dispatchers.IO) {
            if (level == "Все специальности")
                return@withContext database.groups().getAllGroupNamesBy(course).toSortedSet()
            else
                return@withContext database.groups().getGroupNamesBy(level, course).toSortedSet()
        }
    }

}
