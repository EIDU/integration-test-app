package com.eidu.content.test.app.ui.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoadingIndicator() {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxHeight()
                .align(Alignment.CenterHorizontally)
        ) {
            CircularProgressIndicator(
                Modifier
                    .align(Alignment.CenterVertically)
                    .width(100.dp)
                    .height(100.dp)
            )
        }
    }
}