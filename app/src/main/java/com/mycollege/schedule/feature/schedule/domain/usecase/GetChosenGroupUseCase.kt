package com.mycollege.schedule.feature.schedule.domain.usecase

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.app.activity.data.models.Group
import com.mycollege.schedule.core.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Immutable
class GetChosenGroupUseCase @Inject constructor(
    private val database: Database
) {

    suspend fun getByName(name: String): Group? = withContext(Dispatchers.IO) {
        return@withContext database.persistence().getGroupBy(name)
    }

}