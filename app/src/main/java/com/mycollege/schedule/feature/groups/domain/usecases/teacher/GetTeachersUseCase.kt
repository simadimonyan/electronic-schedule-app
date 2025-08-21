package com.mycollege.schedule.feature.groups.domain.usecases.teacher

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.core.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
@Singleton
class GetTeachersUseCase @Inject constructor(
    private val database: Database
) {

    suspend fun getTeachers(department: String): Set<String> {
        return withContext(Dispatchers.IO) {
            val result = mutableSetOf<String>()
            if (!department.equals("Все кафедры")) {
                database.groups().findTeachersBy(department).map {
                    if (!(it.contains("Вакансия") || it.contains("null"))) {
                        result.add(it)
                    }
                }
                result.toSortedSet()
            }
            else {
                database.groups().getTeachers().map {
                    if (!(it.contains("Вакансия") || it.contains("null"))) {
                        result.add(it)
                    }
                }
                result.toSortedSet()
            }
        }
    }

}