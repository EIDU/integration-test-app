package com.eidu.content.test.app.infrastructure

import android.content.Context
import androidx.room.Room
import com.eidu.content.test.app.model.persistence.ContentAppDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "EIDU Content Apps"
        ).build()
    }

    @Provides
    fun contentAppDao(appDatabase: AppDatabase): ContentAppDao = appDatabase.contentAppDao()
}
