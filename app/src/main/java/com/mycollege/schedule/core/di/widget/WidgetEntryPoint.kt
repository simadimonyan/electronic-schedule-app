package com.mycollege.schedule.core.di.widget

import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.core.db.Database
import com.mycollege.schedule.feature.schedule.domain.usecase.GetChosenGroupUseCase
import com.mycollege.schedule.feature.schedule.domain.usecase.GetTeacherUseCase
import com.mycollege.schedule.feature.schedule.domain.usecase.GetTodayScheduleUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun getCacheManager(): CacheManager
    fun getTodayScheduleUseCase(): GetTodayScheduleUseCase
    fun getChosenGroupUseCase(): GetChosenGroupUseCase
    fun getChosenTeacherUseCase(): GetTeacherUseCase
}