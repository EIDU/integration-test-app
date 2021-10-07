package com.eidu.integration.test.app.infrastructure

import androidx.room.Database
import androidx.room.RoomDatabase
import com.eidu.integration.test.app.model.ContentApp
import com.eidu.integration.test.app.model.persistence.ContentAppDao

@Database(entities = [ContentApp::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contentAppDao(): ContentAppDao
}
