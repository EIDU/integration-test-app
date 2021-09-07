package com.eidu.content.test.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eidu.content.test.app.model.ContentApp
import com.eidu.content.test.app.ui.shared.EiduScaffold
import com.eidu.content.test.app.ui.shared.SAMPLE_APP_1

@Composable
fun EditAppScreen(
    contentApp: ContentApp?,
    onSubmit: (contentApp: ContentApp) -> Unit,
    goBack: () -> Unit
) {
    var appState by remember { mutableStateOf(contentApp ?: getDefaultContentApp()) }
    EiduScaffold(
        title = { Text(text = "Add Content App") },
        goBack = goBack
    ) {
        Column(Modifier.fillMaxWidth()) {
            ContentAppTextField(
                value = appState.name,
                onValueChange = { appState = appState.copy(name = it) },
                label = { Text("App Name") },
            )
            ContentAppTextField(
                value = appState.packageName,
                onValueChange = { appState = appState.copy(packageName = it) },
                label = { Text("App package") },
            )
            ContentAppTextField(
                value = appState.launchClass,
                onValueChange = { appState = appState.copy(launchClass = it) },
                label = { Text("Unit launch activity class") },
            )
            ContentAppTextField(
                value = appState.contentProvider,
                onValueChange = { appState = appState.copy(contentProvider = it) },
                label = { Text("Unit content provider URI") },
            )
            ContentAppTextField(
                value = appState.queryAction,
                onValueChange = { appState = appState.copy(queryAction = it) },
                label = { Text("Unit query activity action") },
            )
            Button(
                onClick = {
                    onSubmit(appState)
                    goBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 10.dp),
                enabled = appState.isValid()
            ) {
                if (contentApp != null) Text("Save Changes")
                else Text("Add Content App")
            }
        }
    }
}

@Composable
private fun ContentAppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp, 5.dp)
    )
}

private fun getDefaultContentApp() = ContentApp.empty().copy(contentProvider = "content://")

@Composable
@Preview
private fun AddAppScreenPreview() {
    EditAppScreen(contentApp = null, onSubmit = {}, goBack = {})
}

@Composable
@Preview
private fun EditAppScreenPreview() {
    EditAppScreen(contentApp = SAMPLE_APP_1, onSubmit = {}, goBack = {})
}