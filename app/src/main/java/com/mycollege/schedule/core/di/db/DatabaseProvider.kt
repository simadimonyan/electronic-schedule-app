package com.mycollege.schedule.core.di.db

import android.content.Context
import androidx.room.Room
import com.mycollege.schedule.core.db.Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseProvider {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): Database {
        return Room.databaseBuilder(context.applicationContext,
            Database::class.java,
            "app_database"
        ).build()
    }

}