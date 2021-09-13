package com.eidu.content.sample.app.ui

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
import com.eidu.content.launch.LaunchData
import com.eidu.content.result.LaunchResultData
import com.eidu.content.sample.app.EIDUContentTestAppTheme
import com.eidu.content.sample.app.shared.EiduScaffold
import java.util.Timer
import java.util.TimerTask

class MainActivity : ComponentActivity() {

    private val contentUnitRunViewModel: ContentUnitRunViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val launchData: LaunchData? = try {
            LaunchData.fromLaunchIntent(intent)
        } catch (e: IllegalArgumentException) {
            Log.e("MainActivity", "onCreate: invalid launch intent: $intent", e)
            setResult(RESULT_CANCELED)
            finish()
            null
        }

        setContent {
            EIDUContentTestAppTheme {
                var launchDataState by remember {
                    mutableStateOf(
                        contentUnitRunViewModel.unitResultDataFromLaunchData(
                            launchData
                        )
                    )
                }
                EiduScaffold(title = { Text("Run of ${launchDataState.contentId})") }) {
                    Column {
                        Card(
                            border = BorderStroke(1.dp, Color.LightGray),
                            modifier = Modifier.padding(5.dp)
                        ) {
                            Column {
                                var expanded by remember { mutableStateOf(false) }
                                ListItem(
                                    text = { Text("Launch Data") }
                                )
                                Divider()
                                if (expanded) {
                                    ListItem(
                                        text = { Text(launchDataState.contentId) },
                                        secondaryText = { Text("Content Unit ID") }
                                    )
                                    ListItem(
                                        text = { Text(launchDataState.contentRunId) },
                                        secondaryText = { Text("Content Unit Run ID") }
                                    )
                                    ListItem(
                                        text = { Text(launchDataState.learnerId) },
                                        secondaryText = { Text("Learner ID") }
                                    )
                                    ListItem(
                                        text = { Text(launchDataState.schoolId) },
                                        secondaryText = { Text("School ID") }
                                    )
                                    ListItem(
                                        text = { Text(launchDataState.environment) },
                                        secondaryText = { Text("Environment") }
                                    )
                                    ListItem(
                                        text = { Text("${launchDataState.remainingForegroundTime}") },
                                        secondaryText = { Text("Remaining Foreground Time") }
                                    )
                                    ListItem(
                                        text = { Text("${launchDataState.inactivityTimeout}") },
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
                                            launchDataState =
                                                launchDataState.copy(foregroundTimeInMs = it)
                                        }
                                    }
                                )
                                ListItem(
                                    text = { Text("Response Data") }
                                )
                                Row {
                                    ListItem(
                                        text = { Text("${launchDataState.score}") },
                                        secondaryText = { Text("Score") },
                                        modifier = Modifier.fillMaxWidth(0.3f)
                                    )
                                    Slider(
                                        value = launchDataState.score,
                                        onValueChange = {
                                            launchDataState = launchDataState.copy(score = it)
                                        },
                                        modifier = Modifier
                                            .padding(5.dp, 0.dp)
                                            .fillMaxWidth(1f)
                                    )
                                }
                                ListItem(
                                    text = { Text("${launchDataState.foregroundTimeInMs}") },
                                    secondaryText = { Text("Foreground Time") }
                                )
                                Column(Modifier.selectableGroup()) {
                                    LaunchResultData.RunContentUnitResult.values()
                                        .forEach { result ->
                                            Row(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(56.dp)
                                                    .selectable(
                                                        selected = (result == launchDataState.launchResult),
                                                        onClick = {
                                                            launchDataState =
                                                                launchDataState.copy(launchResult = result)
                                                        },
                                                        role = Role.RadioButton
                                                    )
                                                    .padding(horizontal = 16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = (result == launchDataState.launchResult),
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
                                OutlinedTextField(
                                    value = launchDataState.additionalData ?: "",
                                    onValueChange = {
                                        launchDataState = launchDataState.copy(additionalData = it)
                                    },
                                    label = { Text("Additional Data") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(5.dp)
                                )
                            }
                        }
                        Button(
                            onClick = { sendResult(launchDataState) },
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
        setResult(RESULT_OK, unitResultData.toLaunchResultData().toResultIntent())
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
