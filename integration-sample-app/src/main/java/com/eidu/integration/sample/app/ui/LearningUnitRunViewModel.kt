package com.eidu.integration.sample.app.ui

import androidx.lifecycle.ViewModel
import com.eidu.integration.RunLearningUnitRequest
import com.eidu.integration.RunLearningUnitResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LearningUnitRunViewModel @Inject constructor() : ViewModel() {
    fun resultFromRequest(request: RunLearningUnitRequest?): UnitResultData =
        UnitResultData.fromRequest(request)
}

data class UnitResultData(
    val learningUnitId: String,
    val learningUnitRunId: String,
    val schoolId: String,
    val learnerId: String,
    val stage: String,
    val remainingForegroundTime: Long?,
    val inactivityTimeout: Long?,
    val resultType: RunLearningUnitResult.ResultType = RunLearningUnitResult.ResultType.Success,
    val score: Float = 0.0f,
    val foregroundTimeInMs: Long = 0,
    val errorDetails: String = "Error Details",
    val additionalData: String? = "{ \"unitRating\": \"GOOD\" }"
) {
    fun toResult() = when (resultType) {
        RunLearningUnitResult.ResultType.Success ->
            RunLearningUnitResult.ofSuccess(
                learningUnitId,
                score,
                foregroundTimeInMs,
                additionalData
            )
        RunLearningUnitResult.ResultType.Abort ->
            RunLearningUnitResult.ofAbort(learningUnitId, score, foregroundTimeInMs, additionalData)
        RunLearningUnitResult.ResultType.Error ->
            RunLearningUnitResult.ofError(
                learningUnitId,
                foregroundTimeInMs,
                errorDetails,
                additionalData
            )
        RunLearningUnitResult.ResultType.TimeUp ->
            RunLearningUnitResult.ofTimeUp(
                learningUnitId,
                score,
                foregroundTimeInMs,
                additionalData
            )
        RunLearningUnitResult.ResultType.TimeoutInactivity ->
            RunLearningUnitResult.ofTimeoutInactivity(
                learningUnitId,
                score,
                foregroundTimeInMs,
                additionalData
            )
    }

    companion object {
        fun fromRequest(request: RunLearningUnitRequest?) =
            UnitResultData(
                request?.learningUnitId ?: "No Learning Unit",
                request?.learningUnitRunId ?: "No Learning Unit Run ID",
                request?.schoolId ?: "No School ID",
                request?.learnerId ?: "No Learner ID",
                request?.stage ?: "No Stage",
                resultType = RunLearningUnitResult.ResultType.Success,
                remainingForegroundTime = request?.remainingForegroundTimeInMs,
                inactivityTimeout = request?.inactivityTimeoutInMs,
            )
    }
}
