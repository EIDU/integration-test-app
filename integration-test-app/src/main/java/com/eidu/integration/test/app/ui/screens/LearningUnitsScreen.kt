package com.eidu.integration.test.app.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eidu.content.learningpackages.domain.LearningUnit
import com.eidu.integration.test.app.model.LearningApp
import com.eidu.integration.test.app.ui.shared.EiduScaffold
import com.eidu.integration.test.app.ui.shared.LoadingIndicator
import com.eidu.integration.test.app.ui.shared.SAMPLE_APP_1
import com.eidu.integration.test.app.ui.viewmodel.Result
import com.eidu.integration.test.app.ui.viewmodel.successValue
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LearningUnitsScreen(
    initialUnitExecuted: Boolean,
    learningApp: LearningApp,
    learningUnits: Result<List<LearningUnit>>,
    getUnitIcon: suspend (LearningUnit) -> Bitmap?,
    runUnit: (LearningUnit) -> Unit,
    goBack: () -> Unit
) {
    if (!initialUnitExecuted) learningUnits.successValue?.singleOrNull()?.let(runUnit)
    else EiduScaffold(
        title = { Text(text = "${learningApp.name} Units") },
        goBack = goBack
    ) {
        when (learningUnits) {
            is Result.Success -> LazyColumn {
                items(
                    learningUnits.result.zip(0..Int.MAX_VALUE),
                    { it.first.toString() }
                ) { (unit, index) ->
                    LearningUnitRow(
                        index = index,
                        learningUnit = unit,
                        runBlocking { getUnitIcon(unit) }
                    ) { runUnit(unit) }
                    Divider()
                }
            }
            is Result.Loading ->
                LoadingIndicator("Loading package. This may take a few minutes.")
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
                    RunManualContentUnit(runUnit = runUnit)
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
                    RunManualContentUnit(runUnit = runUnit)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LearningUnitRow(index: Int, learningUnit: LearningUnit, icon: Bitmap?, runUnit: () -> Unit) {
    ListItem(
        Modifier.clickable { runUnit() },
        icon = {
            icon?.asImageBitmap()?.let { Image(it, "Unit icon", Modifier.height(40.dp)) }
                ?: Icon(Icons.Outlined.Gamepad, "No unit icon", Modifier.height(40.dp))
        },
        text = { Text("$index: " + learningUnit.id) },
        secondaryText = {
            Row {
                Text(learningUnit.title ?: "(no title)")
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
            runUnit(LearningUnit(unitId))
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
    LearningUnitsScreen(true, SAMPLE_APP_1, Result.Success(sampleLearningUnits()), { null }, {}, {})
}

@Preview(showBackground = true)
@Composable
private fun LearningUnitListLoadingPreview() {
    LearningUnitsScreen(true, SAMPLE_APP_1, Result.Loading, { null }, {}, {})
}

@Preview(showBackground = true)
@Composable
private fun LearningUnitListNotFoundPreview() {
    LearningUnitsScreen(true, SAMPLE_APP_1, Result.NotFound, { null }, {}, {})
}

@Preview(showBackground = true)
@Composable
private fun LearningUnitListErrorPreview() {
    LearningUnitsScreen(true, SAMPLE_APP_1, Result.Error("Error"), { null }, {}, {})
}

private fun sampleLearningUnits(): List<LearningUnit> = (1..20).map {
    LearningUnit(
        "Unit-$it",
        "sample.png",
        fields = mapOf(
            "title" to "Unit $it: title"
        )
    )
}
