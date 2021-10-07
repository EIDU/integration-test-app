package com.eidu.integration.test.app.infrastructure

import androidx.room.Database
import androidx.room.RoomDatabase
import com.eidu.integration.test.app.model.LearningApp
import com.eidu.integration.test.app.model.persistence.LearningAppDao

@Database(entities = [LearningApp::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun learningAppDao(): LearningAppDao
}
