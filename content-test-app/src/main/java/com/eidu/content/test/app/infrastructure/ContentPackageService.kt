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
import java.io.File
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
        val unitFile = getContentAppUnitFile(contentApp)
        val contentAppVersion = getContentAppVersion(context, contentApp)
        return if (!unitFile.exists()) {
            clipboardService.setPrimaryClip(ClipData.newPlainText("Unit File", unitFile.path))
            Result.Error(
                "Units file ${unitFile.path} does not exist. The path was copied to your " +
                        "clipboard so you can push it using 'adb push content-units.csv ${unitFile.path}'"
            )
        } else if (contentAppVersion == null) {
            Result.Error("Unable to determine content app version")
        } else {
            val contentUnits = readContentUnitsFromFile(unitFile, contentApp, contentAppVersion)
            Result.Success(contentUnits)
        }
    }

    fun loadContentAppFromContentPackage(uri: Uri): ContentApp {
        val extractionDir = extractPackageFile(uri)
        val contentApp = readContentAppMetadata(extractionDir)
        copyPackageContentToInternalFiles(contentApp, extractionDir)
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

    private fun getContentAppUnitFile(contentApp: ContentApp): File {
        val contentPackageDir = getInternalFilesDir(context, contentApp)
        return contentPackageDir.resolve("content-units.csv")
    }

    private fun readContentUnitsFromFile(
        unitFile: File,
        contentApp: ContentApp,
        contentAppVersion: String
    ) = unitFile.readLines()
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

    private fun extractPackageFile(uri: Uri): File {
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
        return extractionDir
    }

    private fun readContentAppMetadata(extractionDir: File): ContentApp {
        val appMetadataJson = extractionDir.resolve("application-metadata.json")
            .readText().let { Json.parseToJsonElement(it) }.jsonObject
        return ContentApp(
            appMetadataJson["applicationName"]?.jsonPrimitive?.content
                ?: error("Malformed application-metadata.json file"),
            appMetadataJson["applicationPackage"]?.jsonPrimitive?.content
                ?: error("Malformed application-metadata.json file"),
            appMetadataJson["unitLaunchActivityClass"]?.jsonPrimitive?.content
                ?: error("Malformed application-metadata.json file")
        )
    }

    private fun copyPackageContentToInternalFiles(
        contentApp: ContentApp,
        extractionDir: File
    ) {
        val internalContentAppDir = getInternalFilesDir(context, contentApp)
        internalContentAppDir.mkdirs()
        extractionDir.copyRecursively(internalContentAppDir, overwrite = true)
        extractionDir.deleteRecursively()
    }
}