package com.mycollege.schedule.feature.schedule.domain.usecase

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.app.activity.data.models.Group
import com.mycollege.schedule.core.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
@Singleton
class GetGroupUseCase @Inject constructor(
    private val database: Database
){

    suspend fun getGroupById(id: String): Group {
        return withContext(Dispatchers.IO) {
            database.groups().getGroupById(id).first()
        }
    }

}