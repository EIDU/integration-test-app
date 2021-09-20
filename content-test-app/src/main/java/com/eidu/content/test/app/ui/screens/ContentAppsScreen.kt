package com.eidu.content.test.app.ui.screens

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
import com.eidu.content.test.app.model.ContentApp
import com.eidu.content.test.app.ui.shared.EiduScaffold
import com.eidu.content.test.app.ui.shared.SAMPLE_APP_1
import com.eidu.content.test.app.ui.shared.SAMPLE_APP_2

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContentAppsScreen(
    contentApps: List<ContentApp>,
    navigateToUnits: (app: ContentApp) -> Unit,
    deleteContentApp: (app: ContentApp) -> Unit,
    openFilePicker: () -> Unit
) {
    EiduScaffold(
        floatingAction = {
            ExtendedFloatingActionButton(
                onClick = openFilePicker,
                text = { Text(text = "Add content package") },
                icon = {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add App")
                }
            )
        },
        title = { Text("Content Apps") }
    ) {
        Column {
            ListItem(
                text = {
                    Text(
                        "Upload your content package to the device (e.g. `adb push content-package.zip" +
                                " /sdcard/Download`) and add your app through 'Add content package'"
                    )
                },
                icon = { Icon(Icons.Default.Info, "How to add content package") }
            )
            Divider()
            LazyColumn {
                items(contentApps, { it.toString() }) {
                    ContentAppRow(
                        contentApp = it,
                        { -> navigateToUnits(it) },
                        { -> deleteContentApp(it) }
                    )
                    Divider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContentAppRow(
    contentApp: ContentApp,
    navigateToUnits: () -> Unit,
    deleteContentApp: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        var dropdownOpen by remember { mutableStateOf(false) }
        ListItem(
            modifier = Modifier.clickable { navigateToUnits() },
            text = { Text(contentApp.name) },
            secondaryText = { Text(contentApp.packageName) },
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
                        DropdownMenuItem(onClick = deleteContentApp) {
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
private fun ContentAppScreenPreview() {
    ContentAppsScreen(
        contentApps = listOf(SAMPLE_APP_1, SAMPLE_APP_2),
        navigateToUnits = {},
        deleteContentApp = {},
        openFilePicker = {}
    )
}
