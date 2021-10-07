package com.eidu.integration.test.app.infrastructure

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.eidu.integration.test.app.model.persistence.LearningAppDao
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
            "EIDU Learning Apps"
        ).addMigrations(
            MIGRATION_1_2
        ).build()
    }

    @Provides
    fun learningAppDao(appDatabase: AppDatabase): LearningAppDao = appDatabase.learningAppDao()

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE content_apps_migration (name TEXT NOT NULL PRIMARY KEY, package TEXT NOT NULL, launch_class TEXT NOT NULL)")
                database.execSQL("INSERT INTO content_apps_migration SELECT name, package, launch_class FROM content_apps")
                database.execSQL("DROP TABLE content_apps")
                database.execSQL("ALTER TABLE content_apps_migration RENAME TO content_apps")
            }
        }
    }
}
