package com.eidu.integration.test.app.ui.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoadingIndicator(message: String? = null) {
    Row(
        Modifier
            .fillMaxHeight()
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
        ) {
            CircularProgressIndicator(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(100.dp)
                    .height(100.dp)
            )

            if (message != null)
                Text(
                    text = message,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
        }
    }
}
