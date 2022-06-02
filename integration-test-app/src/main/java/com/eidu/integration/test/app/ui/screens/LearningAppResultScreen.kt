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
import com.eidu.integration.ResultItem
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
                        fields = listOf(
                            "Result" to resultType.toString(),
                            "Score" to "$score",
                            "Foreground duration" to "$foregroundDurationInMs ms",
                            "Additional data" to "$additionalData",
                            "Error Details" to (if (resultType == RunLearningUnitResult.ResultType.Error) "$errorDetails" else null)
                        ) + (
                            items?.mapIndexed { index, item -> "Item $index" to item.toJson().toString(2) }
                                ?: listOf("Items" to "No item list available.")
                            ).ifEmpty { listOf("Items" to "Item list is empty.") },
                        copyToClipboard
                    )
                }
            is Result.Loading ->
                LoadingIndicator("Loading package. This may take a few minutes.")
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
private fun ResultFields(fields: List<Pair<String, String?>>, copyToClipboard: (String, String) -> Unit) {
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
                1.0f,
                48_735,
                "{ \"numberOfClicks\": 14 }",
                listOf(
                    ResultItem("id1", "1 + 2", "4", "3", 0.5f, 1000, 500),
                    ResultItem(null, null, null, null, null, null, null)
                )
            )
        ),
        { _, _ -> },
        {},
        {}
    )
}

@Composable
@Preview
private fun LearningAppResultScreenWithoutItemListPreview() {
    LearningAppResultScreen(
        SAMPLE_APP_1,
        Result.Success(
            RunLearningUnitResult.ofSuccess(
                1.0f,
                48_735,
                "{ \"numberOfClicks\": 14 }",
                null
            )
        ),
        { _, _ -> },
        {},
        {}
    )
}

@Composable
@Preview
private fun LearningAppResultScreenWithEmptyItemListPreview() {
    LearningAppResultScreen(
        SAMPLE_APP_1,
        Result.Success(
            RunLearningUnitResult.ofSuccess(
                1.0f,
                48_735,
                "{ \"numberOfClicks\": 14 }",
                emptyList()
            )
        ),
        { _, _ -> },
        {},
        {}
    )
}
