package com.eidu.content.test.app.infrastructure

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import com.eidu.content.test.app.model.ContentApp
import com.eidu.content.test.app.model.ContentUnit
import com.eidu.content.test.app.ui.viewmodel.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentPackageService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getContentUnits(
        contentApp: ContentApp,
        clipboardService: ClipboardManager
    ): Result<List<ContentUnit>> {
        val contentPackageDir = getInternalFilesDir(context, contentApp)
        val unitFile = contentPackageDir.resolve("content-units.csv")
        val contentAppVersion = getContentAppVersion(context, contentApp)
        if (!unitFile.exists()) {
            clipboardService.setPrimaryClip(ClipData.newPlainText("Unit File", unitFile.path))
            return Result.Error(
                "Units file ${unitFile.path} does not exist. The path was copied to your " +
                        "clipboard so you can push it using 'adb push content-units.csv ${unitFile.path}'"
            )
        } else if (contentAppVersion == null) {
            return Result.Error("Unable to determine content app version")
        } else {
            val contentUnits = unitFile.readLines()
                .mapIndexedNotNull { index, line ->
                    if (index in (0..1)) null
                    else {
                        val (unitId, icon, _) = line.split(";")
                        ContentUnit(
                            contentApp,
                            contentAppVersion,
                            unitId,
                            icon
                        )
                    }
                }
            return Result.Success(contentUnits)
        }
    }

    fun extractContentPackage(uri: Uri): ContentApp {
        val extractionDir = context.cacheDir.resolve(UUID.randomUUID().toString())
        extractionDir.mkdir()
        context.contentResolver.openInputStream(uri)?.use {
            val stream = ZipInputStream(it)
            var entry = stream.nextEntry
            while (entry != null) {
                if (entry.isDirectory) {
                    Log.i("ContentAppViewModel", "Extracting directory ${entry.name}")
                    extractionDir.resolve(entry.name).mkdirs()
                } else {
                    Log.i("ContentAppViewModel", "Extracting file ${entry.name}")
                    val extractTo = extractionDir.resolve(entry.name)
                    stream.copyTo(extractTo.outputStream())
                }
                stream.closeEntry()
                entry = stream.nextEntry
            }
        }
        val appMetadataJson = extractionDir.resolve("application-metadata.json")
            .readText().let { Json.parseToJsonElement(it) }.jsonObject
        val contentApp = ContentApp(
            appMetadataJson["applicationName"]?.jsonPrimitive?.content
                ?: error("Malformed application-metadata.json file"),
            appMetadataJson["applicationPackage"]?.jsonPrimitive?.content
                ?: error("Malformed application-metadata.json file"),
            appMetadataJson["unitLaunchActivityClass"]?.jsonPrimitive?.content
                ?: error("Malformed application-metadata.json file")
        )
        val internalContentAppDir = getInternalFilesDir(context, contentApp)
        internalContentAppDir.mkdirs()
        extractionDir.copyRecursively(internalContentAppDir, overwrite = true)
        extractionDir.deleteRecursively()
        return contentApp
    }

    private fun getInternalFilesDir(
        context: Context,
        contentApp: ContentApp
    ) = context.filesDir.resolve(contentApp.packageName)

    private fun getContentAppVersion(context: Context, contentApp: ContentApp): String? =
        getContentAppInfo(context, contentApp)?.versionName

    private fun getContentAppInfo(context: Context, contentApp: ContentApp): PackageInfo? =
        try {
            context.packageManager.getPackageInfo(contentApp.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(
                "ContentAppViewModel",
                "getContentAppInfo: unable to query content info for package ${contentApp.packageName}",
                e
            )
            null
        }
}