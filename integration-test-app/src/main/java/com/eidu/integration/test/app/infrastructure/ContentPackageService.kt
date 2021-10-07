package com.eidu.integration.test.app.infrastructure

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import com.eidu.integration.test.app.model.ContentApp
import com.eidu.integration.test.app.model.ContentUnit
import com.eidu.integration.test.app.ui.viewmodel.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
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
            readContentUnitsFromFile(unitFile, contentApp, contentAppVersion)
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
        return contentPackageDir.resolve("content-units.json")
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun readContentUnitsFromFile(
        unitFile: File,
        contentApp: ContentApp,
        contentAppVersion: String
    ) = try {
        unitFile.readText().let {
            Json.decodeFromString<ContentUnitList>(it)
        }.contentUnits.map {
            ContentUnit(
                contentApp,
                contentAppVersion,
                it.unitId,
                it.icon
            )
        }.let { Result.Success(it) }
    } catch (e: Throwable) {
        Log.e("ContentPackageService", "Unable to read units.", e)
        Result.Error("Unable to read units from content-units.json file. Error was: ${e.localizedMessage}")
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

    @OptIn(ExperimentalSerializationApi::class)
    private fun readContentAppMetadata(extractionDir: File): ContentApp {
        val appMetadataJson = extractionDir.resolve("application-metadata.json")
            .readText().let { Json.decodeFromString<ApplicationMetadata>(it) }
        return ContentApp(
            appMetadataJson.applicationName,
            appMetadataJson.applicationPackage,
            appMetadataJson.unitLaunchActivityClass
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

@Serializable
data class ApplicationMetadata(
    val applicationName: String,
    val applicationPackage: String,
    val unitLaunchActivityClass: String
)

@Serializable
data class ContentUnitList(
    val contentUnits: List<ContentUnitDefinition>
)

@Serializable
data class ContentUnitDefinition(
    val unitId: String,
    val icon: String,
    val additionalAssets: List<String>
)
