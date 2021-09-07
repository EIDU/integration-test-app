package com.eidu.content.sample.app.content

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import com.eidu.content.query.QueryContentProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class ContentUnitsProvider : ContentProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ContentUnitsProviderEntryPoint {
        fun contentUnitsDao(): ContentUnitsDao
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val contentUnitsDao = getContentUnitsDao()
        Log.i("ContentUnitsProvider", "query: Content units provider called.")

        val cursor = MatrixCursor(arrayOf(QueryContentProvider.AVAILABLE_UNIT_IDS_COLUMN))
        contentUnitsDao.getContentUnits().forEach {
            cursor.addRow(arrayOf(it.contentUnitId))
        }
        return cursor
    }

    private fun getContentUnitsDao(): ContentUnitsDao {
        val appContext = context?.applicationContext ?: error("Unable to get application context.")
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            appContext,
            ContentUnitsProviderEntryPoint::class.java
        )
        return hiltEntryPoint.contentUnitsDao()
    }

    override fun onCreate(): Boolean = true
    override fun getType(p0: Uri): String? = null
    override fun insert(p0: Uri, p1: ContentValues?): Uri? = null
    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int = -1
    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int = -1
}
