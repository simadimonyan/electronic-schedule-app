package com.mycollege.schedule.feature.schedule.domain.usecase

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.app.activity.data.models.Teacher
import com.mycollege.schedule.core.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Immutable
class GetTeacherUseCase @Inject constructor(
    private val database: Database
) {

    suspend fun getTeacherBy(id: Long): Teacher? = withContext(Dispatchers.IO) {
        return@withContext database.persistence().getTeacherBy(id)
    }

    suspend fun getByName(name: String): Teacher? = withContext(Dispatchers.IO) {
        return@withContext getTeacherBy(database.persistence().findTeacherBy(name))
    }

}