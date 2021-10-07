package com.eidu.integration.test.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.eidu.integration.test.app.model.LearningApp
import com.eidu.integration.test.app.ui.shared.EiduScaffold
import com.eidu.integration.test.app.ui.shared.SAMPLE_APP_1
import com.eidu.integration.test.app.ui.shared.SAMPLE_APP_2

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LearningAppsScreen(
    learningApps: List<LearningApp>,
    navigateToUnits: (app: LearningApp) -> Unit,
    deleteLearningApp: (app: LearningApp) -> Unit,
    openFilePicker: () -> Unit
) {
    EiduScaffold(
        floatingAction = {
            ExtendedFloatingActionButton(
                onClick = openFilePicker,
                text = { Text(text = "Add learning package") },
                icon = {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add App")
                }
            )
        },
        title = { Text("Learning Apps") }
    ) {
        Column {
            ListItem(
                text = {
                    Text(
                        "Upload your learning package to the device (e.g. `adb push learning-package.zip" +
                            " /sdcard/Download`) and add your app through 'Add learning package'"
                    )
                },
                icon = { Icon(Icons.Default.Info, "How to add learning package") }
            )
            Divider()
            LazyColumn {
                items(learningApps, { it.toString() }) {
                    LearningAppRow(
                        learningApp = it,
                        { -> navigateToUnits(it) },
                        { -> deleteLearningApp(it) }
                    )
                    Divider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LearningAppRow(
    learningApp: LearningApp,
    navigateToUnits: () -> Unit,
    deleteLearningApp: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        var dropdownOpen by remember { mutableStateOf(false) }
        ListItem(
            modifier = Modifier.clickable { navigateToUnits() },
            text = { Text(learningApp.name) },
            secondaryText = { Text(learningApp.packageName) },
            trailing = {
                Box {
                    IconButton(onClick = { dropdownOpen = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "More Options"
                        )
                    }
                    DropdownMenu(
                        expanded = dropdownOpen,
                        onDismissRequest = { dropdownOpen = false }
                    ) {
                        DropdownMenuItem(onClick = deleteLearningApp) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete App"
                            )
                            Text("Delete")
                        }
                        DropdownMenuItem(onClick = navigateToUnits) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowForward,
                                contentDescription = "Show Units"
                            )
                            Text("Units")
                        }
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true, device = Devices.NEXUS_5)
@Composable
private fun LearningAppScreenPreview() {
    LearningAppsScreen(
        learningApps = listOf(SAMPLE_APP_1, SAMPLE_APP_2),
        navigateToUnits = {},
        deleteLearningApp = {},
        openFilePicker = {}
    )
}
