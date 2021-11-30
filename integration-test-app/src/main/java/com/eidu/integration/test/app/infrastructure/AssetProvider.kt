package com.eidu.integration.test.app.infrastructure

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.webkit.MimeTypeMap
import com.eidu.integration.test.app.model.LearningApp
import com.eidu.integration.test.app.model.LearningUnit
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.io.FileNotFoundException

class AssetProvider : ContentProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HiltEntryPoint {
        fun learningPackageService(): LearningPackageService
    }

    override fun onCreate(): Boolean = true

    override fun getType(uri: Uri): String =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            (uri.path ?: "").substringAfterLast(".")
        ) ?: "application/octet-stream"

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != "r")
            throw SecurityException("Invalid mode $mode; only reading is allowed.")
        return ParcelFileDescriptor.open(getFileForUri(uri), ParcelFileDescriptor.MODE_READ_ONLY)
    }

    private fun getFileForUri(uri: Uri): File {
        val appContext = context?.applicationContext ?: throw IllegalStateException()

        val hiltEntryPoint = EntryPointAccessors.fromApplication(appContext, HiltEntryPoint::class.java)

        val learningAppPackage = uri.pathSegments.firstOrNull() ?: throw FileNotFoundException("No path in $uri")
        val filePath = uri.pathSegments.drop(1).joinToString("/")

        return hiltEntryPoint.learningPackageService().getAsset(learningAppPackage, filePath)
            ?: throw FileNotFoundException(uri.toString())
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor = throw java.lang.UnsupportedOperationException("Query is not supported.")

    override fun insert(uri: Uri, values: ContentValues?): Uri =
        throw UnsupportedOperationException("Insertion is not supported.")

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int = throw UnsupportedOperationException("Update is not supported.")

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int =
        throw UnsupportedOperationException("Deletion is not supported.")

    companion object {
        fun assetBaseUri(app: LearningApp, unit: LearningUnit): Uri = Uri.Builder()
            .scheme("content")
            .authority("com.eidu.integration.test.app.assets")
            .path(app.packageName)
            .appendQueryParameter("unit", unit.unitId)
            .build()
    }
}
