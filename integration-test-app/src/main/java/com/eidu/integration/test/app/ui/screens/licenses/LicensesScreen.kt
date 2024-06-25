package com.eidu.integration.test.app.ui.screens.licenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.eidu.integration.test.app.ui.shared.EiduScaffold
import com.eidu.integration.test.app.ui.viewmodel.LicensesViewModel

@Composable
fun LicensesScreen(
    goBack: () -> Unit,
    navController: NavHostController,
    licensesViewModel: LicensesViewModel
) {
    EiduScaffold(
        title = { Text("Open Source Licenses") },
        goBack = goBack
    ) {
        LazyColumn {
            items(licensesViewModel.dependencies.size) { index ->
                val dependency = licensesViewModel.dependencies[index]
                Column(
                    Modifier
                        .clickable { navController.navigate("licenses/$index") }
                        .padding(10.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        "${dependency.name} ${dependency.version}",
                        fontWeight = FontWeight.Bold
                    )
                    dependency.licenses.forEach { Text(it.name) }
                }
            }
        }
    }
}
