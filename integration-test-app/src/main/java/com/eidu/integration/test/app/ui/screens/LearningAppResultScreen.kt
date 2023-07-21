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
import com.eidu.integration.test.app.ui.shared.EiduScaffold
import com.eidu.integration.test.app.ui.shared.LearningAppErrorDisplay
import com.eidu.integration.test.app.ui.shared.LoadingIndicator
import com.eidu.integration.test.app.ui.viewmodel.Result

@Composable
fun LearningAppResultScreen(
    learningAppResult: Result<RunLearningUnitResult>,
    copyToClipboard: (String, String) -> Unit,
    goToEditScreen: () -> Unit,
    goBack: () -> Unit,
    finish: () -> Unit
) {
    EiduScaffold(
        title = { Text("Learning App Result") },
        goBack = goBack
    ) {
        when (learningAppResult) {
            is Result.Success ->
                finish()
            is Result.Loading ->
                LoadingIndicator("Loading package. This may take a few minutes.")
            is Result.Error ->
                LearningAppErrorDisplay(
                    error = learningAppResult,
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
        {},
        {}
    )
}

@Composable
@Preview
private fun LearningAppResultScreenWithoutItemListPreview() {
    LearningAppResultScreen(
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
        {},
        {}
    )
}

@Composable
@Preview
private fun LearningAppResultScreenWithEmptyItemListPreview() {
    LearningAppResultScreen(
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
        {},
        {}
    )
}
