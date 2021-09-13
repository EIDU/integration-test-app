package com.eidu.content.test.app.service

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import com.eidu.content.query.QueryContentProvider
import com.eidu.content.test.app.model.ContentApp
import com.eidu.content.test.app.model.ContentUnit
import com.eidu.content.test.app.model.QuerySource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentQueryService @Inject constructor() {
    fun queryContentProvider(contentApp: ContentApp, context: Context): List<ContentUnit>? {
        val contentAppVersion = getContentAppInfo(contentApp, context)?.versionName
            ?: return null
        val contentProviderUri = Uri.parse(contentApp.contentProvider)

        Log.i(
            "ContentQueryService",
            "queryContentProvider: Calling content provider of app ${contentApp.packageName}" +
                " version $contentAppVersion with URI $contentProviderUri"
        )

        return context.contentResolver.query(
            contentProviderUri,
            null,
            null,
            null,
            null
        )
            ?.use {
                QueryContentProvider.getContentIds(it)
            }?.map {
                ContentUnit(contentApp, contentAppVersion, it, QuerySource.ContentProvider)
            }
    }

    fun getContentAppVersion(contentApp: ContentApp, context: Context): String? =
        getContentAppInfo(contentApp, context)?.versionName

    private fun getContentAppInfo(contentApp: ContentApp, context: Context): PackageInfo? =
        try {
            context.packageManager.getInstalledPackages(0)
                .forEach { Log.i("ContentQueryService", "getContentAppInfo: ${it.packageName}") }
            context.packageManager.getPackageInfo(contentApp.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(
                "ContentQueryService",
                "getContentAppInfo: unable to query content info for package ${contentApp.packageName}",
                e
            )
            null
        }
}
