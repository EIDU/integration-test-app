package com.eidu.content.test.app.ui.screens

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
import com.eidu.content.result.LaunchResultData
import com.eidu.content.test.app.model.ContentApp
import com.eidu.content.test.app.ui.shared.ContentAppErrorDisplay
import com.eidu.content.test.app.ui.shared.EiduScaffold
import com.eidu.content.test.app.ui.shared.LoadingIndicator
import com.eidu.content.test.app.ui.shared.SAMPLE_APP_1
import com.eidu.content.test.app.ui.viewmodel.Result

@Composable
fun ContentAppResultScreen(
    contentApp: ContentApp,
    contentAppResult: Result<LaunchResultData>,
    copyToClipboard: (String, String) -> Unit,
    goToEditScreen: () -> Unit,
    goBack: () -> Unit
) {
    EiduScaffold(
        title = { Text("Content App Result") },
        goBack = goBack
    ) {
        when (contentAppResult) {
            is Result.Success ->
                with(contentAppResult.result) {
                    ResultFields(
                        fields = mapOf(
                            "Content ID" to contentId,
                            "Result" to runContentUnitResult.toString(),
                            "Score" to "$score",
                            "Foreground duration" to "$foregroundDurationInMs",
                            "Additional data" to "$additionalData"
                        ),
                        copyToClipboard
                    )
                }
            is Result.Loading ->
                LoadingIndicator()
            is Result.Error ->
                ContentAppErrorDisplay(
                    error = contentAppResult,
                    contentApp = contentApp,
                    navigateToEditScreen = goToEditScreen
                )
            is Result.NotFound ->
                Text(text = "This should not happen here.")
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ResultFields(fields: Map<String, String>, copyToClipboard: (String, String) -> Unit) {
    Column {
        fields.map { (key, value) ->
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

@Composable
@Preview
private fun ContentAppResultScreenPreview() {
    ContentAppResultScreen(
        SAMPLE_APP_1,
        Result.Success(
            LaunchResultData.fromPlainData(
                "03.EIDU.FishTank",
                LaunchResultData.RunContentUnitResult.Success,
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
