package com.eidu.integration.test.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eidu.integration.test.app.model.LearningApp
import com.eidu.integration.test.app.model.LearningUnit
import com.eidu.integration.test.app.ui.shared.EiduScaffold
import com.eidu.integration.test.app.ui.shared.LoadingIndicator
import com.eidu.integration.test.app.ui.shared.SAMPLE_APP_1
import com.eidu.integration.test.app.ui.viewmodel.Result

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LearningUnitsScreen(
    learningApp: LearningApp,
    learningUnits: Result<List<LearningUnit>>,
    runUnit: (LearningUnit) -> Unit,
    goBack: () -> Unit
) {
    EiduScaffold(
        title = { Text(text = "${learningApp.name} Units") },
        goBack = goBack
    ) {
        when (learningUnits) {
            is Result.Success -> LazyColumn {
                items(learningUnits.result, { it.toString() }) {
                    LearningUnitRow(learningUnit = it) { runUnit(it) }
                    Divider()
                }
            }
            is Result.Loading ->
                LoadingIndicator()
            is Result.NotFound ->
                Column {
                    ListItem(
                        text = { Text("No units found for app. You can try to run a unit manually below.") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "Warning, No Units Found"
                            )
                        }
                    )
                    RunManualContentUnit(learningApp = learningApp, runUnit = runUnit)
                }

            is Result.Error -> {
                Column {
                    ListItem(
                        text = { Text("Unable to read unit file for app. You can try to run a unit manually below.") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "Warning, No Units Found"
                            )
                        }
                    )
                    RunManualContentUnit(learningApp = learningApp, runUnit = runUnit)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LearningUnitRow(learningUnit: LearningUnit, runUnit: () -> Unit) {
    ListItem(
        Modifier.clickable { runUnit() },
        text = { Text(learningUnit.unitId) },
        secondaryText = {
            Row {
                Text(learningUnit.learningApp.name, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.width(5.dp))
                Text(learningUnit.learningAppVersion)
            }
        },
        trailing = {
            IconButton(onClick = runUnit) {
                Icon(Icons.Filled.PlayArrow, "Start Unit")
            }
        }
    )
}

@Composable
private fun RunManualContentUnit(
    learningApp: LearningApp,
    runUnit: (LearningUnit) -> Unit
) {
    var unitId by remember { mutableStateOf("") }
    OutlinedTextField(
        value = unitId,
        onValueChange = { unitId = it },
        label = { Text("Learning Unit Id") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp, 5.dp)
    )
    Button(
        onClick = {
            runUnit(LearningUnit(learningApp, "1.0", unitId, ""))
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp, 10.dp)
    ) {
        Text("Run Learning Unit")
    }
}

@Preview(showBackground = true)
@Composable
private fun LearningUnitListPreview() {
    LearningUnitsScreen(SAMPLE_APP_1, Result.Success(sampleLearningUnits()), {}, {})
}

@Preview(showBackground = true)
@Composable
private fun LearningUnitListLoadingPreview() {
    LearningUnitsScreen(SAMPLE_APP_1, Result.Loading, {}, {})
}

@Preview(showBackground = true)
@Composable
private fun LearningUnitListNotFoundPreview() {
    LearningUnitsScreen(SAMPLE_APP_1, Result.NotFound, {}, {})
}

@Preview(showBackground = true)
@Composable
private fun LearningUnitListErrorPreview() {
    LearningUnitsScreen(SAMPLE_APP_1, Result.Error("Error"), {}, {})
}

private fun sampleLearningUnits(): List<LearningUnit> = (1..20).map {
    LearningUnit(SAMPLE_APP_1, "1.7.23", "Learning-Unit-$it", "sample.png")
}
