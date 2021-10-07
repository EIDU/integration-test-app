package com.eidu.integration.sample.app.ui

import androidx.lifecycle.ViewModel
import com.eidu.content.integration.RunContentUnitRequest
import com.eidu.content.integration.RunContentUnitResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ContentUnitRunViewModel @Inject constructor() : ViewModel() {
    fun resultFromRequest(request: RunContentUnitRequest?): UnitResultData =
        UnitResultData.fromRequest(request)
}

data class UnitResultData(
    val contentId: String,
    val contentRunId: String,
    val schoolId: String,
    val learnerId: String,
    val stage: String,
    val remainingForegroundTime: Long?,
    val inactivityTimeout: Long?,
    val resultType: RunContentUnitResult.ResultType = RunContentUnitResult.ResultType.Success,
    val score: Float = 0.0f,
    val foregroundTimeInMs: Long = 0,
    val errorDetails: String = "Error Details",
    val additionalData: String? = "{ \"unitRating\": \"GOOD\" }"
) {
    fun toResult() = when (resultType) {
        RunContentUnitResult.ResultType.Success ->
            RunContentUnitResult.ofSuccess(contentId, score, foregroundTimeInMs, additionalData)
        RunContentUnitResult.ResultType.Abort ->
            RunContentUnitResult.ofAbort(contentId, foregroundTimeInMs, additionalData)
        RunContentUnitResult.ResultType.Error ->
            RunContentUnitResult.ofError(contentId, foregroundTimeInMs, errorDetails, additionalData)
        RunContentUnitResult.ResultType.TimeUp ->
            RunContentUnitResult.ofTimeUp(contentId, foregroundTimeInMs, additionalData)
        RunContentUnitResult.ResultType.TimeoutInactivity ->
            RunContentUnitResult.ofTimeoutInactivity(contentId, foregroundTimeInMs, additionalData)
    }

    companion object {
        fun fromRequest(request: RunContentUnitRequest?) =
            UnitResultData(
                request?.contentId ?: "No Content Unit",
                request?.contentUnitRunId ?: "No Content Unit Run ID",
                request?.schoolId ?: "No School ID",
                request?.learnerId ?: "No Learner ID",
                request?.stage ?: "No Stage",
                resultType = RunContentUnitResult.ResultType.Success,
                remainingForegroundTime = request?.remainingForegroundTimeInMs,
                inactivityTimeout = request?.inactivityTimeoutInMs,
            )
    }
}
