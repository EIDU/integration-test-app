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
        learningAppPackage: String,
        clipboardService: ClipboardManager
    ): Result<List<LearningUnit>> {
        val unitFile = getLearningAppUnitFile(learningAppPackage)
        val learningAppVersion = getLearningAppVersion(context, learningAppPackage)
        return if (!unitFile.exists()) {
            clipboardService.setPrimaryClip(ClipData.newPlainText("Unit File", unitFile.path))
            Result.Error(
                "Units file ${unitFile.path} does not exist. Have you uploaded a complete and correct learning package?"
            )
        } else if (learningAppVersion == null) {
            Result.Error("Unable to determine learning app version")
        } else {
            readLearningUnitsFromFile(unitFile, learningAppPackage, learningAppVersion)
        }
    }

    fun getAsset(learningAppPackage: String, filePath: String): File? =
        getAssetsDir(learningAppPackage)
            ?.resolve(filePath)
            ?.takeIf { it.isFile }

    private fun getAssetsDir(learningAppPackage: String): File? =
        getInternalFilesDir(context, learningAppPackage)
            .resolve("assets")
            .takeIf { it.exists() }

    fun loadLearningAppFromLearningPackage(uri: Uri): LearningApp {
        val extractionDir = extractPackageFile(uri)
        val learningApp = readLearningAppMetadata(extractionDir)
        copyPackageContentToInternalFiles(learningApp.packageName, extractionDir)
        return learningApp
    }

    private fun getInternalFilesDir(
        context: Context,
        learningAppPackage: String
    ) = context.filesDir.resolve(learningAppPackage)

    private fun getLearningAppVersion(context: Context, learningAppPackage: String): String? =
        getLearningAppInfo(context, learningAppPackage)?.versionName

    private fun getLearningAppInfo(context: Context, learningAppPackage: String): PackageInfo? =
        try {
            context.packageManager.getPackageInfo(learningAppPackage, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(
                "LearningAppViewModel",
                "getLearningAppInfo: unable to query learning app info for package $learningAppPackage",
                e
            )
            null
        }

    private fun getLearningAppUnitFile(learningAppPackage: String): File {
        val learningPackageDir = getInternalFilesDir(context, learningAppPackage)
        return learningPackageDir.resolve("learning-units.json")
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun readLearningUnitsFromFile(
        unitFile: File,
        learningAppPackage: String,
        learningAppVersion: String
    ) = try {
        unitFile.readText().let {
            Json.decodeFromString<LearningUnitList>(it)
        }.learningUnits.map {
            LearningUnit(
                learningAppPackage,
                learningAppVersion,
                it.unitId,
                it.icon
            )
        }.let { Result.Success(it) }
    } catch (e: Throwable) {
        Log.e("LearningPackageService", "Unable to read units.", e)
        Result.Error("Unable to read units from learning-units.json file. Error was: ${e.localizedMessage}")
    }

    private fun extractPackageFile(uri: Uri): File {
        val extractionDir = context.cacheDir.resolve(UUID.randomUUID().toString())
        extractionDir.mkdir()
        context.contentResolver.openInputStream(uri)?.use {
            val stream = ZipInputStream(it)
            var entry = stream.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    Log.i("LearningPackageService", "Extracting file ${entry.name}")
                    val extractTo = extractionDir.resolve(entry.name)
                    extractTo.parentFile?.mkdirs()
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
        learningAppPackage: String,
        extractionDir: File
    ) {
        val internalLearningAppDir = getInternalFilesDir(context, learningAppPackage)
        internalLearningAppDir.deleteRecursively()
        internalLearningAppDir.mkdirs()
        extractionDir.copyRecursively(internalLearningAppDir, overwrite = true)
        extractionDir.deleteRecursively()
    }
}

@Serializable
data class ApplicationMetadata(
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
