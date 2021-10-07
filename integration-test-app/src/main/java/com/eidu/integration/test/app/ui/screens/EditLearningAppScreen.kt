package com.eidu.integration.test.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eidu.integration.test.app.model.LearningApp
import com.eidu.integration.test.app.ui.shared.EiduScaffold
import com.eidu.integration.test.app.ui.shared.SAMPLE_APP_1

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EditLearningAppScreen(
    learningApp: LearningApp?,
    onSubmit: (app: LearningApp) -> Unit,
    goBack: () -> Unit
) {
    EiduScaffold(
        title = { Text(text = if (learningApp == null) "Add Learning App" else "Edit ${learningApp.name}") },
        goBack = goBack
    ) {
        var appState by remember { mutableStateOf(learningApp ?: LearningApp("", "", "")) }
        Column(Modifier.fillMaxWidth()) {
            LearningAppTextField(
                value = appState.name,
                onValueChange = { appState = appState.copy(name = it) },
                label = { Text("App name") },
            )
            LearningAppTextField(
                value = appState.packageName,
                onValueChange = { appState = appState.copy(packageName = it) },
                label = { Text("App package") },
            )
            LearningAppTextField(
                value = appState.launchClass,
                onValueChange = { appState = appState.copy(launchClass = it) },
                label = { Text("Unit launch activity class") },
            )
            Button(
                onClick = {
                    onSubmit(appState)
                    goBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 10.dp)
            ) {
                if (learningApp != null) Text("Save Changes")
                else Text("Add Learning App")
            }
        }
    }
}

@Composable
private fun LearningAppTextField(
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

@Composable
@Preview
private fun AddAppScreenPreview() {
    EditLearningAppScreen(learningApp = null, onSubmit = {}, goBack = {})
}

@Composable
@Preview
private fun EditAppScreenPreview() {
    EditLearningAppScreen(learningApp = SAMPLE_APP_1, onSubmit = {}, goBack = {})
}
