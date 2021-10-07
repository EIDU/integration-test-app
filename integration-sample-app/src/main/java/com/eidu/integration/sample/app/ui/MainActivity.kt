package com.eidu.integration.sample.app.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.eidu.content.integration.RunContentUnitRequest
import com.eidu.content.integration.RunContentUnitResult
import com.eidu.integration.sample.app.EIDUContentTestAppTheme
import com.eidu.integration.sample.app.shared.EiduScaffold
import java.util.Timer
import java.util.TimerTask

class MainActivity : ComponentActivity() {

    private val contentUnitRunViewModel: ContentUnitRunViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val request: RunContentUnitRequest? = try {
            RunContentUnitRequest.fromIntent(intent)
        } catch (e: IllegalArgumentException) {
            Log.e("MainActivity", "onCreate: invalid launch intent: $intent", e)
            setResult(
                RESULT_CANCELED,
                RunContentUnitResult.ofError(
                    "unknown",
                    0L,
                    "Invalid Intent received: $intent",
                    null
                ).toIntent()
            )
            finish()
            null
        }

        setContent {
            EIDUContentTestAppTheme {
                var requestDataState by remember {
                    mutableStateOf(
                        contentUnitRunViewModel.resultFromRequest(
                            request
                        )
                    )
                }
                EiduScaffold(title = { Text("Run of ${requestDataState.contentId}") }) {
                    Column {
                        Card(
                            border = BorderStroke(1.dp, Color.LightGray),
                            modifier = Modifier.padding(5.dp)
                        ) {
                            Column {
                                var expanded by remember { mutableStateOf(false) }
                                ListItem(
                                    text = { Text("Request Data") }
                                )
                                Divider()
                                if (expanded) {
                                    ListItem(
                                        text = { Text(requestDataState.contentId) },
                                        secondaryText = { Text("Content Unit ID") }
                                    )
                                    ListItem(
                                        text = { Text(requestDataState.contentRunId) },
                                        secondaryText = { Text("Content Unit Run ID") }
                                    )
                                    ListItem(
                                        text = { Text(requestDataState.learnerId) },
                                        secondaryText = { Text("Learner ID") }
                                    )
                                    ListItem(
                                        text = { Text(requestDataState.schoolId) },
                                        secondaryText = { Text("School ID") }
                                    )
                                    ListItem(
                                        text = { Text(requestDataState.stage) },
                                        secondaryText = { Text("Stage") }
                                    )
                                    ListItem(
                                        text = { Text("${requestDataState.remainingForegroundTime}") },
                                        secondaryText = { Text("Remaining Foreground Time") }
                                    )
                                    ListItem(
                                        text = { Text("${requestDataState.inactivityTimeout}") },
                                        secondaryText = { Text("Inactivity Timeout") }
                                    )
                                    Divider()
                                }
                                TextButton(onClick = { expanded = !expanded }) {
                                    Text(if (expanded) "Collapse" else "Expand")
                                }
                            }
                        }
                        Card(
                            border = BorderStroke(1.dp, Color.LightGray),
                            modifier = Modifier.padding(5.dp)
                        ) {
                            Column {
                                LaunchedEffect(
                                    key1 = true,
                                    block = {
                                        foregroundTimeTimer {
                                            requestDataState =
                                                requestDataState.copy(foregroundTimeInMs = it)
                                        }
                                    }
                                )
                                ListItem(
                                    text = { Text("Result Data") }
                                )
                                Column(Modifier.selectableGroup()) {
                                    RunContentUnitResult.ResultType.values()
                                        .forEach { result ->
                                            Row(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(56.dp)
                                                    .selectable(
                                                        selected = (result == requestDataState.resultType),
                                                        onClick = {
                                                            requestDataState =
                                                                requestDataState.copy(resultType = result)
                                                        },
                                                        role = Role.RadioButton
                                                    )
                                                    .padding(horizontal = 16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = (result == requestDataState.resultType),
                                                    onClick = null
                                                )
                                                Text(
                                                    text = result.toString(),
                                                    style = MaterialTheme.typography.body1.merge(),
                                                    modifier = Modifier.padding(start = 16.dp)
                                                )
                                            }
                                        }
                                }
                                if (requestDataState.resultType == RunContentUnitResult.ResultType.Success) {
                                    Row {
                                        ListItem(
                                            text = { Text("${requestDataState.score}") },
                                            secondaryText = { Text("Score") },
                                            modifier = Modifier.fillMaxWidth(0.3f)
                                        )
                                        Slider(
                                            value = requestDataState.score,
                                            onValueChange = {
                                                requestDataState = requestDataState.copy(score = it)
                                            },
                                            modifier = Modifier
                                                .padding(5.dp, 0.dp)
                                                .fillMaxWidth(1f)
                                        )
                                    }
                                }
                                ListItem(
                                    text = { Text("${requestDataState.foregroundTimeInMs}") },
                                    secondaryText = { Text("Foreground Time") }
                                )
                                if (requestDataState.resultType == RunContentUnitResult.ResultType.Error) {
                                    OutlinedTextField(
                                        value = requestDataState.errorDetails,
                                        onValueChange = {
                                            requestDataState =
                                                requestDataState.copy(errorDetails = it)
                                        },
                                        label = { Text("Error Details") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(5.dp)
                                    )
                                }
                                OutlinedTextField(
                                    value = requestDataState.additionalData ?: "",
                                    onValueChange = {
                                        requestDataState =
                                            requestDataState.copy(additionalData = it)
                                    },
                                    label = { Text("Additional Data") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(5.dp)
                                )
                            }
                        }
                        Button(
                            onClick = { sendResult(requestDataState) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp)
                        ) {
                            Text("Send Result")
                        }
                    }
                }
            }
        }
    }

    private fun sendResult(unitResultData: UnitResultData) {
        setResult(RESULT_OK, unitResultData.toResult().toIntent())
        finish()
    }

    private fun foregroundTimeTimer(updateState: (Long) -> Unit) =
        Timer().schedule(
            object : TimerTask() {
                val startTime = System.currentTimeMillis()
                override fun run() {
                    updateState(System.currentTimeMillis() - startTime)
                }
            },
            0L,
            1000L
        )
}
