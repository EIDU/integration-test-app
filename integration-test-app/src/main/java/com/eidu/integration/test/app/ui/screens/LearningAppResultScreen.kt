package com.eidu.integration.test.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.eidu.integration.RunLearningUnitResult
import com.eidu.integration.test.app.model.LearningApp
import com.eidu.integration.test.app.ui.shared.EiduScaffold
import com.eidu.integration.test.app.ui.shared.LearningAppErrorDisplay
import com.eidu.integration.test.app.ui.shared.LoadingIndicator
import com.eidu.integration.test.app.ui.shared.SAMPLE_APP_1
import com.eidu.integration.test.app.ui.viewmodel.Result

@Composable
fun LearningAppResultScreen(
    learningApp: LearningApp,
    learningAppResult: Result<RunLearningUnitResult>,
    copyToClipboard: (String, String) -> Unit,
    goToEditScreen: () -> Unit,
    goBack: () -> Unit
) {
    EiduScaffold(
        title = { Text("Learning App Result") },
        goBack = goBack
    ) {
        when (learningAppResult) {
            is Result.Success ->
                with(learningAppResult.result) {
                    ResultFields(
                        fields = mapOf(
                            "Learning Unit ID" to learningUnitId,
                            "Result" to resultType.toString(),
                            "Score" to (if (resultType != RunLearningUnitResult.ResultType.Error) "$score" else null),
                            "Foreground duration" to "$foregroundDurationInMs",
                            "Additional data" to "$additionalData",
                            "Error Details" to (if (resultType == RunLearningUnitResult.ResultType.Error) "$errorDetails" else null)
                        ),
                        copyToClipboard
                    )
                }
            is Result.Loading ->
                LoadingIndicator()
            is Result.Error ->
                LearningAppErrorDisplay(
                    error = learningAppResult,
                    learningApp = learningApp,
                    navigateToEditScreen = goToEditScreen
                )
            is Result.NotFound ->
                Text(text = "This should not happen here.")
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ResultFields(fields: Map<String, String?>, copyToClipboard: (String, String) -> Unit) {
    Column {
        fields.map { (key, value) ->
            if (value != null) {
                ListItem(
                    text = { Text(value) },
                    secondaryText = { Text(key) },
                    trailing = {
                        IconButton(onClick = { copyToClipboard(key, value) }) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy value of $key to clipboard"
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
@Preview
private fun LearningAppResultScreenPreview() {
    LearningAppResultScreen(
        SAMPLE_APP_1,
        Result.Success(
            RunLearningUnitResult.ofSuccess(
                "03.EIDU.FishTank",
                1.0f,
                48_735,
                "{ \"numberOfClicks\": 14 }"
            )
        ),
        { _, _ -> },
        {},
        {}
    )
}
