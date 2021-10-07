package com.eidu.integration.test.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "content_apps")
data class LearningApp(
    @PrimaryKey @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "package") val packageName: String,
    @ColumnInfo(name = "launch_class") val launchClass: String
)
