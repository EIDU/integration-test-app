package com.eidu.content.test.app.ui.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eidu.content.test.app.model.ContentApp
import com.eidu.content.test.app.ui.viewmodel.Result

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContentAppErrorDisplay(
    error: Result.Error,
    contentApp: ContentApp,
    navigateToEditScreen: () -> Unit
) {
    Column {
        ListItem(
            text = { Text(error.reason) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error"
                )
            }
        )
        Divider()
        Button(
            onClick = navigateToEditScreen,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp, 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Go To Edit Screen"
            )
            Text("Edit App")
        }
    }
}

@Preview
@Composable
private fun ErrorPreview() {
    EiduScaffold {
        ContentAppErrorDisplay(
            error = Result.Error("Unable to do stuff."),
            SAMPLE_APP_1
        ) {}
    }
}