package com.eidu.integration.test.app.infrastructure

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import com.eidu.integration.test.app.model.LearningApp
import com.eidu.integration.test.app.model.LearningUnit
import com.eidu.integration.test.app.ui.viewmodel.Result
import com.eidu.integration.test.app.util.getStrings
import com.eidu.integration.test.app.util.parseXml
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.dongliu.apk.parser.ApkFile
import org.w3c.dom.Document
import java.io.File
import java.util.UUID
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LearningPackageService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: LearningAppRepository
) {

    fun getLearningUnits(learningAppPackage: String): List<LearningUnit> =
        repository.getLearningUnits(learningAppPackage)

    fun getAsset(learningAppPackage: String, filePath: String, unitId: String): File? =
        if (repository.findUnit(learningAppPackage, unitId)?.allowsAsset(filePath) == true)
            getAssetsDir(learningAppPackage)?.resolve(filePath)?.takeIf { it.isFile }
        else
            null

    private fun getAssetsDir(learningAppPackage: String): File? =
        getInternalFilesDir(context, learningAppPackage)
            .resolve("assets")
            .takeIf { it.exists() }

    fun putLearningPackage(uri: Uri): Result<Unit> = try {
        val extractionDir = extractPackageFile(uri)
        val learningApp = readLearningAppMetadata(extractionDir)
        copyPackageContentToInternalFiles(learningApp.packageName, extractionDir)

        val result = readLearningUnitsFromFile(
            getLearningAppUnitFile(learningApp.packageName),
            learningApp.packageName
        )

        when (result) {
            is Result.Success<List<LearningUnit>> -> {
                repository.replaceLearningUnits(
                    learningApp.packageName,
                    result.result
                )

                repository.put(learningApp)
                Result.Success(Unit)
            }
            is Result.Error -> result
            else -> error("Unknown error.")
        }
    } catch (e: Exception) {
        Result.Error("Failed to read learning package: $e")
    }

    private fun getInternalFilesDir(
        context: Context,
        learningAppPackage: String
    ) = context.filesDir.resolve(learningAppPackage)

    private fun getLearningAppUnitFile(learningAppPackage: String): File {
        val learningPackageDir = getInternalFilesDir(context, learningAppPackage)
        return learningPackageDir.resolve("learning-units.json")
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun readLearningUnitsFromFile(
        unitFile: File,
        learningAppPackage: String
    ) = try {
        unitFile.readText().let {
            Json.decodeFromString<LearningUnitList>(it)
        }.learningUnits.map {
            LearningUnit(
                learningAppPackage,
                it.unitId,
                it.icon,
                it.additionalAssets
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
        val file = extractionDir.listFiles { _, fileName -> fileName.endsWith("apk") }
            ?.singleOrNull() ?: error("Not exactly one APK found.")

        return parseApk(file)
    }

    private fun parseApk(apk: File) = parseManifest(parseXml(ApkFile(apk).manifestXml))

    private fun parseManifest(manifest: Document): LearningApp {
        val packageName = manifest.getStrings("/manifest/@package").single()
        val label = manifest.getStrings("/manifest/application/@label").single()
        val launchUnitActivity = manifest.getStrings(
            "/manifest/application/activity" +
                "[intent-filter/action/@name='com.eidu.integration.LAUNCH_LEARNING_UNIT']/@name"
        ).singleOrNull()
            ?: error("Did not find exactly one activity with an intent filter for action 'com.eidu.integration.LAUNCH_LEARNING_UNIT'.")

        return LearningApp(label, packageName, launchUnitActivity)
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

    fun listLive(): LiveData<List<LearningApp>> = repository.listLive()
    fun put(learningApp: LearningApp) = repository.put(learningApp)
    fun delete(learningApp: LearningApp) = repository.delete(learningApp)
    fun findByPackageName(name: String): LearningApp? = repository.findByPackageName(name)
}

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
