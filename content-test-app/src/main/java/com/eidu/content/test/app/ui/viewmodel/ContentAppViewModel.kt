package com.eidu.content.test.app.ui.viewmodel

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.eidu.content.integration.RunContentUnitRequest
import com.eidu.content.integration.RunContentUnitResult
import com.eidu.content.test.app.infrastructure.ContentPackageService
import com.eidu.content.test.app.model.ContentApp
import com.eidu.content.test.app.model.ContentUnit
import com.eidu.content.test.app.model.persistence.ContentAppDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID
import java.util.zip.ZipInputStream
import javax.inject.Inject

@HiltViewModel
class ContentAppViewModel @Inject constructor(
    private val contentAppDao: ContentAppDao,
    private val contentPackageService: ContentPackageService
) : ViewModel() {

    private val _contentUnits = MutableLiveData<Result<List<ContentUnit>>>(Result.Loading)
    private val _contentAppResult = MutableLiveData<Result<RunContentUnitResult>>()

    fun getContentApps(): LiveData<List<ContentApp>> = contentAppDao.getAll()

    fun upsertContentApp(contentApp: ContentApp) =
        viewModelScope.launch(Dispatchers.IO) {
            contentAppDao.upsert(contentApp)
        }

    fun deleteContentApp(contentApp: ContentApp) =
        viewModelScope.launch(Dispatchers.IO) {
            contentAppDao.delete(contentApp)
        }

    fun getContentAppByName(name: String): LiveData<Result<ContentApp>> {
        val data = MutableLiveData<Result<ContentApp>>(Result.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val app = contentAppDao.findByName(name)
            if (app != null) {
                data.postValue(Result.Success(app))
            } else {
                data.postValue(Result.NotFound)
            }
        }
        return data
    }

    fun loadUnitsFromCSVFile(
        contentApp: ContentApp,
        clipboardService: ClipboardManager
    ): LiveData<Result<List<ContentUnit>>> {
        _contentUnits.postValue(Result.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            _contentUnits.postValue(
                contentPackageService.getContentUnits(
                    contentApp,
                    clipboardService
                )
            )
        }
        return _contentUnits
    }

    fun processUnitRunResult(activityResult: ActivityResult) {
        when {
            activityResult.resultCode != Activity.RESULT_OK ->
                _contentAppResult.postValue(Result.Error("Unexpected result code: ${activityResult.resultCode}"))
            activityResult.data == null ->
                _contentAppResult.postValue(Result.Error("Result intent was null."))
            else -> {
                val resultIntent = activityResult.data ?: error("This shouldn't be null here.")
                try {
                    val resultData = RunContentUnitResult.fromIntent(resultIntent)
                    _contentAppResult.postValue(Result.Success(resultData))
                } catch (e: IllegalArgumentException) {
                    _contentAppResult.postValue(
                        Result.Error(
                            "There was an error parsing the result intent: ${e.localizedMessage}." +
                                    "The resulting intent was: $resultIntent"
                        )
                    )
                }
            }
        }
    }

    fun handleContentPackageFile(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
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
            val internalContentAppDir =
                contentPackageService.getInternalFilesDir(context, contentApp)
            internalContentAppDir.mkdirs()
            extractionDir.copyRecursively(internalContentAppDir, overwrite = true)
            extractionDir.deleteRecursively()
            upsertContentApp(contentApp)
        }
    }

    fun getContentAppResult(): LiveData<Result<RunContentUnitResult>> = _contentAppResult

    fun launchContentAppUnit(
        context: Context,
        contentApp: ContentApp,
        contentUnit: ContentUnit,
        contentAppLauncher: ActivityResultLauncher<Intent>,
        navController: NavController
    ) {
        clearContentAppResult()
        val launchIntent = getLaunchIntent(
            contentApp,
            contentUnit
        )
        navController.navigate("content-apps/${contentApp.name}/result")
        if (context.packageManager.resolveActivity(launchIntent, 0) != null) {
            contentAppLauncher.launch(launchIntent)
        } else {
            Log.w(
                "ContentAppViewModel",
                "launchContentAppUnit: content unit launch activity not found."
            )
            _contentAppResult.postValue(
                Result.Error(
                    "Unable to launch content unit ${contentUnit.unitId} because the activity" +
                            " ${contentApp.packageName}/${contentApp.launchClass} could not be found. " +
                            "Have you declared it in your AndroidManifest.xml file?"
                )
            )
        }
    }

    private fun getLaunchIntent(contentApp: ContentApp, contentUnit: ContentUnit) =
        RunContentUnitRequest.of(
            contentUnit.unitId,
            "Test Run",
            "Test Learner",
            "Test School",
            "test",
            null,
            null
        ).toIntent(contentApp.packageName, contentApp.launchClass)

    private fun clearContentAppResult() {
        _contentAppResult.postValue(Result.Loading)
    }
}

sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<out T>(val result: T) : Result<T>()
    object NotFound : Result<Nothing>()
    data class Error(val reason: String) : Result<Nothing>()
}
