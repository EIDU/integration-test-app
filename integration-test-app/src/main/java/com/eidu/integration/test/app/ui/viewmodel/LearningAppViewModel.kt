package com.eidu.integration.test.app.ui.viewmodel

import android.app.Activity
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
import com.eidu.integration.RunLearningUnitRequest
import com.eidu.integration.RunLearningUnitResult
import com.eidu.integration.test.app.infrastructure.AssetProvider
import com.eidu.integration.test.app.infrastructure.LearningPackageService
import com.eidu.integration.test.app.model.LearningApp
import com.eidu.integration.test.app.model.LearningUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LearningAppViewModel @Inject constructor(
    private val learningPackageService: LearningPackageService
) : ViewModel() {

    private val _learningAppResult = MutableLiveData<Result<RunLearningUnitResult>>()

    private val _importStatus = MutableLiveData<Result<Unit>>()
    val importStatus: LiveData<Result<Unit>> = _importStatus

    fun getLearningApps(): LiveData<List<LearningApp>> = learningPackageService.listLive()

    fun putLearningApp(learningApp: LearningApp) =
        viewModelScope.launch(Dispatchers.IO) {
            learningPackageService.put(learningApp)
        }

    fun deleteLearningApp(learningApp: LearningApp) =
        viewModelScope.launch(Dispatchers.IO) {
            learningPackageService.delete(learningApp)
        }

    fun getLearningAppByPackageName(name: String): LiveData<Result<LearningApp>> {
        val data = MutableLiveData<Result<LearningApp>>(Result.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val app = learningPackageService.findByPackageName(name)
            if (app != null) {
                data.postValue(Result.Success(app))
            } else {
                data.postValue(Result.NotFound)
            }
        }
        return data
    }

    fun getLearningUnitsByPackageName(name: String): LiveData<Result<List<LearningUnit>>> {
        val data = MutableLiveData<Result<List<LearningUnit>>>(Result.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val units = learningPackageService.getLearningUnits(name).takeIf { it.isNotEmpty() }
            if (units != null)
                data.postValue(Result.Success(units))
            else
                data.postValue(Result.NotFound)
        }
        return data
    }

    fun processUnitRunResult(activityResult: ActivityResult) {
        when {
            activityResult.resultCode != Activity.RESULT_OK ->
                _learningAppResult.postValue(Result.Error("Unexpected result code: ${activityResult.resultCode}"))
            activityResult.data == null ->
                _learningAppResult.postValue(Result.Error("Result intent was null."))
            else -> {
                val resultIntent = activityResult.data ?: error("This shouldn't be null here.")
                try {
                    val resultData = RunLearningUnitResult.fromIntent(resultIntent)
                    _learningAppResult.postValue(Result.Success(resultData))
                } catch (e: IllegalArgumentException) {
                    _learningAppResult.postValue(
                        Result.Error(
                            "There was an error parsing the result intent: ${e.localizedMessage}." +
                                "The resulting intent was: $resultIntent"
                        )
                    )
                }
            }
        }
    }

    fun handleLearningPackageFile(uri: Uri) {
        _importStatus.postValue(Result.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            _importStatus.postValue(learningPackageService.putLearningPackage(uri))
        }
    }

    fun getLearningAppResult(): LiveData<Result<RunLearningUnitResult>> = _learningAppResult

    fun launchLearningAppUnit(
        learningApp: LearningApp,
        learningUnit: LearningUnit,
        learningAppLauncher: ActivityResultLauncher<Intent>,
        navController: NavController
    ) {
        clearLearningAppResult()
        val launchIntent = getLaunchIntent(
            learningApp,
            learningUnit
        )
        navController.navigate("learning-apps/${learningApp.packageName}/result")
        try {
            learningAppLauncher.launch(launchIntent)
        } catch (e: Exception) {
            Log.w(
                "LearningAppViewModel",
                "launchLearningAppUnit: learning unit launch activity not found.",
                e
            )
            _learningAppResult.postValue(
                Result.Error(
                    "Unable to launch learning unit ${learningUnit.unitId} using activity" +
                        " ${learningApp.packageName}/${learningApp.launchClass} - details: $e"
                )
            )
        }
    }

    private fun getLaunchIntent(learningApp: LearningApp, learningUnit: LearningUnit) =
        RunLearningUnitRequest.of(
            learningUnit.unitId,
            "Test Run",
            "Test Learner",
            "Test School",
            "test",
            null,
            null,
            AssetProvider.assetBaseUri(learningApp, learningUnit)
        ).toIntent(learningApp.packageName, learningApp.launchClass)

    private fun clearLearningAppResult() {
        _learningAppResult.postValue(Result.Loading)
    }
}

sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<out T>(val result: T) : Result<T>()
    object NotFound : Result<Nothing>()
    data class Error(val reason: String) : Result<Nothing>()
}
