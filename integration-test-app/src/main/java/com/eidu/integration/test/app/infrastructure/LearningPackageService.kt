package com.eidu.integration.test.app.infrastructure

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.LiveData
import com.eidu.content.learningpackages.LearningPackage
import com.eidu.content.learningpackages.domain.LearningUnit
import com.eidu.integration.test.app.model.LearningApp
import com.eidu.integration.test.app.ui.viewmodel.Result
import com.eidu.integration.test.app.util.AsyncCache
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LearningPackageService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: LearningAppRepository
) {
    private val learningPackageCache = AsyncCache<String, LearningPackage>(
        3,
        { withContext(Dispatchers.IO) { LearningPackage(internalLearningPackageFile(it)) } },
        { it.close() }
    )

    suspend fun getLearningPackage(learningAppPackage: String): LearningPackage =
        learningPackageCache.get(learningAppPackage)

    suspend fun getIcon(learningAppPackage: String, unit: LearningUnit): Bitmap? =
        getLearningPackage(learningAppPackage).icons[unit.icon]?.read()?.let {
            BitmapFactory.decodeStream(it)
        }

    fun putLearningPackage(uri: Uri): Result<Unit> = try {
        val tempFile = context.cacheDir.resolve("import.zip").also {
            it.outputStream().use { output ->
                context.contentResolver.openInputStream(uri).use { input ->
                    input!!.copyTo(output)
                }
            }
        }
        try {
            val meta = LearningPackage(tempFile).use {
                validateLearningPackage(it)
                it.meta
            }

            storeLearningPackage(meta.app.appId, tempFile)

            repository.put(LearningApp(meta.app.appId, meta.app.appId, meta.launchUnitActivity))
        } finally {
            tempFile.delete()
        }
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error("Failed to read learning package: $e")
    }

    private fun storeLearningPackage(appId: String, file: File) =
        file.copyTo(internalLearningPackageFile(appId), overwrite = true)

    private fun validateLearningPackage(learningPackage: LearningPackage) {
        learningPackage.learningUnitList
        learningPackage.assets
        learningPackage.icons
    }

    fun listLive(): LiveData<List<LearningApp>> = repository.listLive()
    fun list(): List<LearningApp> = repository.list()
    fun put(learningApp: LearningApp) = repository.put(learningApp)
    fun delete(learningApp: LearningApp) = repository.delete(learningApp)
    fun findByPackageName(name: String): LearningApp? = repository.findByPackageName(name)

    private fun internalLearningPackageFile(packageName: String): File =
        internalLearningPackageDirectory().resolve("$packageName.zip")

    private fun internalLearningPackageDirectory(): File =
        context.filesDir.resolve("packages").also { it.mkdirs() }
}
