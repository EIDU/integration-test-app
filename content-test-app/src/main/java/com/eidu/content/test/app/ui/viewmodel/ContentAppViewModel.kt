package com.eidu.content.test.app.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.eidu.content.launch.LaunchData
import com.eidu.content.result.LaunchResultData
import com.eidu.content.test.app.model.ContentApp
import com.eidu.content.test.app.model.ContentUnit
import com.eidu.content.test.app.model.persistence.ContentAppDao
import com.eidu.content.test.app.service.ContentQueryService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContentAppViewModel @Inject constructor(
    private val contentAppDao: ContentAppDao,
    private val contentQueryService: ContentQueryService
) : ViewModel() {

    private val _contentUnits = MutableLiveData<Result<List<ContentUnit>>>(Result.Loading)
    private val _contentAppResult = MutableLiveData<Result<LaunchResultData>>()

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

    fun queryUnitsByContentProvider(
        context: Context,
        contentApp: ContentApp
    ): LiveData<Result<List<ContentUnit>>> {
        _contentUnits.postValue(Result.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val queryResult = contentQueryService.queryContentProvider(contentApp, context)
                ?.let { Result.Success(it) }
                ?: Result.Error("Could not query content provider because it was not found.")
            _contentUnits.postValue(queryResult)
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
                    val resultData = LaunchResultData.fromResultIntent(resultIntent)
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

    fun getContentAppResult(): LiveData<Result<LaunchResultData>> = _contentAppResult

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
        LaunchData.fromPlainData(
            contentUnit.unitId,
            "Test Run",
            "Test Learner",
            "Test School",
            "test",
            null,
            null
        ).toLaunchIntent(contentApp.packageName, contentApp.launchClass)

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
