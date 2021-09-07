package com.eidu.content.sample.app.ui

import androidx.lifecycle.ViewModel
import com.eidu.content.launch.LaunchData
import com.eidu.content.result.LaunchResultData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ContentUnitRunViewModel @Inject constructor() : ViewModel() {
    fun unitResultDataFromLaunchData(launchData: LaunchData?): UnitResultData =
        UnitResultData.fromLaunchData(launchData)
}

data class UnitResultData(
    val contentId: String,
    val contentRunId: String,
    val schoolId: String,
    val learnerId: String,
    val environment: String,
    val remainingForegroundTime: Long?,
    val inactivityTimeout: Long?,
    val launchResult: LaunchResultData.RunContentUnitResult = LaunchResultData.RunContentUnitResult.Success,
    val score: Float = 0.0f,
    val foregroundTimeInMs: Long = 0,
    val additionalData: String? = "{ \"unitRating\": \"GOOD\" }"
) {
    fun toLaunchResultData() = LaunchResultData.fromPlainData(
        contentId,
        launchResult ?: LaunchResultData.RunContentUnitResult.Error,
        score,
        foregroundTimeInMs,
        additionalData
    )

    companion object {
        fun fromLaunchData(launchData: LaunchData?) =
            UnitResultData(
                launchData?.contentId ?: "No Content Unit",
                launchData?.contentUnitRunId ?: "No Content Unit Run ID",
                launchData?.schoolId ?: "No School ID",
                launchData?.learnerId ?: "No Learner ID",
                launchData?.environment ?: "No Environment",
                launchResult = LaunchResultData.RunContentUnitResult.Success,
                remainingForegroundTime = launchData?.remainingForegroundTimeInMs,
                inactivityTimeout = launchData?.inactivityTimeoutInMs,
            )
    }
}
