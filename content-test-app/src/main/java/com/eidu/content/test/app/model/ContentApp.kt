package com.eidu.content.test.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "content_apps")
data class ContentApp(
    @PrimaryKey @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "package") val packageName: String,
    @ColumnInfo(name = "launch_class") val launchClass: String,
    @ColumnInfo(name = "query_action") val queryAction: String,
    @ColumnInfo(name = "content_provider") val contentProvider: String
) {
    fun isValid(): Boolean =
        name.isNotBlank() &&
            packageName.isNotBlank() &&
            launchClass.isNotBlank()

    companion object {
        fun empty(): ContentApp = ContentApp(
            "", "", "", "", ""
        )
    }
}
