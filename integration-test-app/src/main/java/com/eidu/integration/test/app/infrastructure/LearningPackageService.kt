package com.eidu.integration.test.app.infrastructure

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import com.eidu.integration.test.app.model.LearningApp
import com.eidu.integration.test.app.model.LearningUnit
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
class LearningPackageService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getLearningUnits(
        learningApp: LearningApp,
        clipboardService: ClipboardManager
    ): Result<List<LearningUnit>> {
        val unitFile = getLearningAppUnitFile(learningApp)
        val learningAppVersion = getLearningAppVersion(context, learningApp)
        return if (!unitFile.exists()) {
            clipboardService.setPrimaryClip(ClipData.newPlainText("Unit File", unitFile.path))
            Result.Error(
                "Units file ${unitFile.path} does not exist. Have you uploaded a complete and correct learning package?"
            )
        } else if (learningAppVersion == null) {
            Result.Error("Unable to determine learning app version")
        } else {
            readLearningUnitsFromFile(unitFile, learningApp, learningAppVersion)
        }
    }

    fun loadLearningAppFromLearningPackage(uri: Uri): LearningApp {
        val extractionDir = extractPackageFile(uri)
        val learningApp = readLearningAppMetadata(extractionDir)
        copyPackageContentToInternalFiles(learningApp, extractionDir)
        return learningApp
    }

    private fun getInternalFilesDir(
        context: Context,
        learningApp: LearningApp
    ) = context.filesDir.resolve(learningApp.packageName)

    private fun getLearningAppVersion(context: Context, learningApp: LearningApp): String? =
        getLearningAppInfo(context, learningApp)?.versionName

    private fun getLearningAppInfo(context: Context, learningApp: LearningApp): PackageInfo? =
        try {
            context.packageManager.getPackageInfo(learningApp.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(
                "LearningAppViewModel",
                "getLearningAppInfo: unable to query learning app info for package ${learningApp.packageName}",
                e
            )
            null
        }

    private fun getLearningAppUnitFile(learningApp: LearningApp): File {
        val learningPackageDir = getInternalFilesDir(context, learningApp)
        return learningPackageDir.resolve("content-units.json")
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun readLearningUnitsFromFile(
        unitFile: File,
        learningApp: LearningApp,
        learningAppVersion: String
    ) = try {
        unitFile.readText().let {
            Json.decodeFromString<LearningUnitList>(it)
        }.learningUnits.map {
            LearningUnit(
                learningApp,
                learningAppVersion,
                it.unitId,
                it.icon
            )
        }.let { Result.Success(it) }
    } catch (e: Throwable) {
        Log.e("LearningPackageService", "Unable to read units.", e)
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
                    Log.i("LearningPackageService", "Extracting directory ${entry.name}")
                    extractionDir.resolve(entry.name).mkdirs()
                } else {
                    Log.i("LearningPackageService", "Extracting file ${entry.name}")
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
    private fun readLearningAppMetadata(extractionDir: File): LearningApp {
        val appMetadataJson = extractionDir.resolve("application-metadata.json")
            .readText().let { Json.decodeFromString<ApplicationMetadata>(it) }
        val applicationName =
            extractionDir.list { _, fileName -> fileName.endsWith("apk") }?.firstOrNull()?.let {
                getApplicationLabelFromApk(extractionDir.resolve(it).path)
            }
        return LearningApp(
            applicationName ?: "Unknown Application",
            appMetadataJson.applicationPackage,
            appMetadataJson.unitLaunchActivityClass
        )
    }

    private fun getApplicationLabelFromApk(apkFilePath: String): String? {
        return context.packageManager.getPackageArchiveInfo(apkFilePath, 0)?.also {
            it.applicationInfo.sourceDir = apkFilePath
            it.applicationInfo.publicSourceDir = apkFilePath
        }?.let {
            context.packageManager.getApplicationLabel(it.applicationInfo).toString()
        }
    }

    private fun copyPackageContentToInternalFiles(
        learningApp: LearningApp,
        extractionDir: File
    ) {
        val internalLearningAppDir = getInternalFilesDir(context, learningApp)
        internalLearningAppDir.mkdirs()
        extractionDir.copyRecursively(internalLearningAppDir, overwrite = true)
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
data class LearningUnitList(
    val learningUnits: List<LearningUnitDefinition>
)

@Serializable
data class LearningUnitDefinition(
    val unitId: String,
    val icon: String,
    val additionalAssets: List<String>
)
