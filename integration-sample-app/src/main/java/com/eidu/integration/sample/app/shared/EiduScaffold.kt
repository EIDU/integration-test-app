package com.eidu.integration.sample.app.shared

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable

@Composable
fun EiduScaffold(
    title: @Composable () -> Unit = { Text(text = "EIDU Learning Sample App") },
    floatingAction: @Composable () -> Unit = {},
    bottomBarActions: @Composable (RowScope.() -> Unit)? = null,
    goBack: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = title,
                navigationIcon = goBack?.let {
                    {
                        IconButton(onClick = it) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Go Back"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = floatingAction,
        bottomBar = {
            if (bottomBarActions != null) {
                BottomAppBar {
                    bottomBarActions(this)
                }
            }
        }
    ) {
        content()
    }
}
