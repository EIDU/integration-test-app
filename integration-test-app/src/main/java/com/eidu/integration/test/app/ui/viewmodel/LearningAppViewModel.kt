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
import androidx.navigation.NavOptions
import com.eidu.content.learningpackages.domain.LearningUnit
import com.eidu.integration.RunLearningUnitRequest
import com.eidu.integration.RunLearningUnitResult
import com.eidu.integration.test.app.infrastructure.AssetProvider
import com.eidu.integration.test.app.infrastructure.LearningPackageService
import com.eidu.integration.test.app.model.LearningApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LearningAppViewModel @Inject constructor(
    private val learningPackageService: LearningPackageService
) : ViewModel() {

    private val _learningAppResult = MutableLiveData<Result<RunLearningUnitResult>>()

    private val _importStatus = MutableLiveData<Result<Unit>>()
    val importStatus: LiveData<Result<Unit>> = _importStatus

    val requestedUnitLaunch = MutableLiveData<String?>()

    fun dismissStatus() {
        _importStatus.postValue(null)
    }

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
            try {
                data.postValue(Result.Success(learningPackageService.getLearningPackage(name).learningUnitList.units))
            } catch (e: Exception) {
                data.postValue(Result.Error(e.message ?: "Unknown error"))
            }
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

    @OptIn(FlowPreview::class)
    fun launchLearningAppUnit(
        unitId: String,
        learningAppLauncher: ActivityResultLauncher<Intent>,
        navController: NavController
    ) = viewModelScope.launch {
        val learningAppAndUnit = findUnit(unitId)

        if (learningAppAndUnit != null) {
            val (learningApp, learningUnit) = learningAppAndUnit

            launchLearningAppUnit(
                learningApp,
                learningUnit,
                learningAppLauncher,
                navController
            )
        }
    }

    @OptIn(FlowPreview::class)
    private suspend fun findUnit(unitId: String) =
        learningPackageService.list().asFlow().flatMapConcat { app ->
            learningPackageService.getLearningPackage(app.packageName).learningUnitList.units.asFlow()
                .map { app to it }
        }.firstOrNull { (_, unit) -> unit.id == unitId }

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
        navController.navigate("learning-apps/${learningApp.packageName}/result", NavOptions.Builder().setLaunchSingleTop(true).build())
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
                    "Unable to launch learning unit ${learningUnit.id} using activity" +
                        " ${learningApp.packageName}/${learningApp.launchClass} - details: $e"
                )
            )
        }
    }

    private fun getLaunchIntent(learningApp: LearningApp, learningUnit: LearningUnit) =
        RunLearningUnitRequest.of(
            learningUnit.id,
            "Test Run",
            "Test Learner",
            "Test School",
            "test",
            5 * 60 * 1000L,
            1 * 60 * 1000L,
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
