package com.mycollege.schedule.core.di

import android.content.Context
import com.mycollege.schedule.shared.resources.ResourceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ContextModule {

    @Provides
    fun provideResourceManager(@ApplicationContext context: Context): ResourceManager {
        return ResourceManager(context)
    }

}