package com.eidu.content.test.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eidu.content.test.app.model.ContentApp
import com.eidu.content.test.app.model.ContentUnit
import com.eidu.content.test.app.model.QuerySource
import com.eidu.content.test.app.ui.shared.ContentAppErrorDisplay
import com.eidu.content.test.app.ui.shared.EiduScaffold
import com.eidu.content.test.app.ui.shared.LoadingIndicator
import com.eidu.content.test.app.ui.shared.SAMPLE_APP_1
import com.eidu.content.test.app.ui.viewmodel.Result

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContentUnitsScreen(
    contentApp: ContentApp,
    contentUnits: Result<List<ContentUnit>>,
    runUnit: (ContentUnit) -> Unit,
    requeryByProvider: () -> Unit,
    requeryByIntent: () -> Unit,
    goToEditScreen: () -> Unit,
    goBack: () -> Unit
) {
    EiduScaffold(
        title = { Text(text = "${contentApp.name} Units") },
        bottomBarActions = {
            Spacer(Modifier.weight(1f, true))
            IconButton(onClick = requeryByProvider) {
                Icon(Icons.Filled.Refresh, contentDescription = "Query By Provider")
            }
            IconButton(onClick = requeryByIntent) {
                Icon(Icons.Filled.ExitToApp, contentDescription = "Query By Intent")
            }
        },
        goBack = goBack
    ) {
        when (contentUnits) {
            is Result.Success -> LazyColumn {
                items(contentUnits.result, { it.toString() }) {
                    ContentUnitRow(contentUnit = it) { runUnit(it) }
                    Divider()
                }
            }
            is Result.Loading ->
                LoadingIndicator()
            is Result.NotFound ->
                ListItem(
                    text = { Text("No units found for app. Did your app return them?") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Warning, No Units Found"
                        )
                    }
                )
            is Result.Error ->
                ContentAppErrorDisplay(
                    error = contentUnits,
                    contentApp = contentApp,
                    navigateToEditScreen = goToEditScreen
                )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContentUnitRow(contentUnit: ContentUnit, runUnit: () -> Unit) {
    ListItem(
        Modifier.clickable { runUnit() },
        text = { Text(contentUnit.unitId) },
        secondaryText = {
            Row {
                Text(contentUnit.contentApp.name, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.width(5.dp))
                Text(contentUnit.contentAppVersion)
            }
        },
        overlineText = {
            Text("Source: ${contentUnit.querySource.name}")
        },
        trailing = {
            IconButton(onClick = runUnit) {
                Icon(Icons.Filled.PlayArrow, "Start Unit")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ContentUnitListPreview() {
    ContentUnitsScreen(SAMPLE_APP_1, Result.Success(sampleContentUnits()), {}, {}, {}, {}, {})
}

@Preview(showBackground = true)
@Composable
private fun ContentUnitListLoadingPreview() {
    ContentUnitsScreen(SAMPLE_APP_1, Result.Loading, {}, {}, {}, {}, {})
}

@Preview(showBackground = true)
@Composable
private fun ContentUnitListNotFoundPreview() {
    ContentUnitsScreen(SAMPLE_APP_1, Result.NotFound, {}, {}, {}, {}, {})
}

@Preview(showBackground = true)
@Composable
private fun ContentUnitListErrorPreview() {
    ContentUnitsScreen(SAMPLE_APP_1, Result.Error("Error"), {}, {}, {}, {}, {})
}

private fun sampleContentUnits(): List<ContentUnit> = (1..20).map {
    ContentUnit(SAMPLE_APP_1, "1.7.23", "Content-Unit-$it", QuerySource.ContentProvider)
}
