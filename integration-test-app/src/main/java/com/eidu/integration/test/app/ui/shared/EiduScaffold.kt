package com.eidu.integration.test.app.ui.shared

import androidx.compose.foundation.layout.PaddingValues
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
    title: @Composable () -> Unit = { Text(text = "EIDU Integration Test App") },
    floatingAction: @Composable () -> Unit = {},
    bottomBarActions: @Composable (RowScope.() -> Unit)? = null,
    goBack: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
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
        content(it)
    }
}
