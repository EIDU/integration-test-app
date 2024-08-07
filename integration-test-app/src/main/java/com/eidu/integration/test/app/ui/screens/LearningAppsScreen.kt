package com.eidu.integration.test.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.eidu.integration.test.app.model.LearningApp
import com.eidu.integration.test.app.ui.shared.EiduScaffold
import com.eidu.integration.test.app.ui.shared.LoadingIndicator
import com.eidu.integration.test.app.ui.shared.SAMPLE_APP_1
import com.eidu.integration.test.app.ui.shared.SAMPLE_APP_2
import com.eidu.integration.test.app.ui.viewmodel.Result

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LearningAppsScreen(
    learningApps: List<LearningApp>,
    importStatus: LiveData<Result<Unit>?>,
    dismissStatus: () -> Unit,
    navigateToUnits: (app: LearningApp) -> Unit,
    deleteLearningApp: (app: LearningApp) -> Unit,
    editLearningApp: (app: LearningApp) -> Unit,
    openFilePicker: () -> Unit,
    addLearningApp: () -> Unit,
    navigateToOpenSourceLicenses: () -> Unit
) {
    val currentStatus = importStatus.observeAsState().value

    EiduScaffold(
        floatingAction = {
            var addOptionsOpen by remember { mutableStateOf(false) }
            if (currentStatus != Result.Loading)
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    if (addOptionsOpen) {
                        ExtendedFloatingActionButton(
                            onClick = openFilePicker,
                            text = { Text(text = "Add learning package") },
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Add package"
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        ExtendedFloatingActionButton(
                            onClick = addLearningApp,
                            text = { Text(text = "Add manually") },
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Add manually"
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                    FloatingActionButton(
                        onClick = { addOptionsOpen = !addOptionsOpen },
                    ) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add App")
                    }
                }
        },
        title = { Text("Learning Apps") }
    ) {
        when (currentStatus) {
            Result.Loading -> LoadingIndicator("Loading package. This may take a few minutes.")
            Result.NotFound -> error("Unexpected import status: NotFound")
            is Result.Error ->
                AlertDialog(
                    onDismissRequest = dismissStatus,
                    confirmButton = {
                        TextButton(onClick = dismissStatus) { Text(text = "OK") }
                    },
                    title = { Text(text = "Error") },
                    text = { Text(text = currentStatus.reason) }
                )
            is Result.Success ->
                AlertDialog(
                    onDismissRequest = dismissStatus,
                    confirmButton = {
                        TextButton(onClick = dismissStatus) { Text(text = "OK") }
                    },
                    title = { Text(text = "Success") },
                    text = { Text(text = "Package imported successfully.") }
                )
            null -> Column {
                ListItem(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = {
                        Column() {
                            Text(
                                "Upload your learning package to this device (e.g. `adb push learning-package.zip" +
                                        " /sdcard/`) and add it via 'Add learning package', or" +
                                        " add an app manually if you don't have a learning package yet.\n\n" +
                                        "Note: You need to install the APK yourself!",
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "View open source licenses",
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colors.primary,
                                modifier = Modifier.clickable { navigateToOpenSourceLicenses() }
                            )
                        }
                    },
                    icon = { Icon(Icons.Default.Info, "How to add a learning package") }
                )
                Divider()
                LazyColumn {
                    items(learningApps, { it.toString() }) {
                        LearningAppRow(
                            learningApp = it,
                            { -> navigateToUnits(it) },
                            { -> deleteLearningApp(it) },
                            { -> editLearningApp(it) }
                        )
                        Divider()
                    }
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
    deleteLearningApp: () -> Unit,
    editLearningApp: () -> Unit
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
                        DropdownMenuItem(onClick = editLearningApp) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit App"
                            )
                            Text("Edit")
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
        importStatus = MutableLiveData(null),
        dismissStatus = {},
        navigateToUnits = {},
        deleteLearningApp = {},
        editLearningApp = {},
        openFilePicker = {},
        addLearningApp = {},
        navigateToOpenSourceLicenses = {}
    )
}

@Preview(showBackground = true, device = Devices.NEXUS_5)
@Composable
private fun LearningAppScreenPreviewLoading() {
    LearningAppsScreen(
        learningApps = listOf(SAMPLE_APP_1, SAMPLE_APP_2),
        importStatus = MutableLiveData(Result.Loading),
        dismissStatus = {},
        navigateToUnits = {},
        deleteLearningApp = {},
        editLearningApp = {},
        openFilePicker = {},
        addLearningApp = {},
        navigateToOpenSourceLicenses = {}
    )
}

@Preview(showBackground = true, device = Devices.NEXUS_5)
@Composable
private fun LearningAppScreenPreviewSuccess() {
    LearningAppsScreen(
        learningApps = listOf(SAMPLE_APP_1, SAMPLE_APP_2),
        importStatus = MutableLiveData(Result.Success(Unit)),
        dismissStatus = {},
        navigateToUnits = {},
        deleteLearningApp = {},
        editLearningApp = {},
        openFilePicker = {},
        addLearningApp = {},
        navigateToOpenSourceLicenses = {}
    )
}

@Preview(showBackground = true, device = Devices.NEXUS_5)
@Composable
private fun LearningAppScreenPreviewError() {
    LearningAppsScreen(
        learningApps = listOf(SAMPLE_APP_1, SAMPLE_APP_2),
        importStatus = MutableLiveData(Result.Error("You fail it.")),
        dismissStatus = {},
        navigateToUnits = {},
        deleteLearningApp = {},
        editLearningApp = {},
        openFilePicker = {},
        addLearningApp = {},
        navigateToOpenSourceLicenses = {}
    )
}
