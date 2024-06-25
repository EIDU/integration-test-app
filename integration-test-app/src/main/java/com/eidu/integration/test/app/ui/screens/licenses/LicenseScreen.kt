package com.eidu.integration.test.app.ui.screens.licenses

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import com.eidu.integration.test.app.ui.shared.EiduScaffold
import com.eidu.integration.test.app.ui.viewmodel.LicensesViewModel

@Composable
fun LicenseScreen(
    backStackEntry: NavBackStackEntry,
    goBack: () -> Unit,
    licensesViewModel: LicensesViewModel,
    openUri: (Uri) -> Unit
) {
    val dependency =
        licensesViewModel.dependencies[backStackEntry.arguments?.getInt(
            "index"
        )!!]
    EiduScaffold(
        title = { Text("${dependency.name} ${dependency.version}") },
        goBack = goBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(10.dp)
        ) {
            Text(
                dependency.description,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Text(
                dependency.url,
                fontSize = 11.sp,
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .clickable { openUri(Uri.parse(dependency.url)) },
            )

            dependency
                .licenses.forEach {
                    Spacer(modifier = Modifier.height(36.dp))
                    Text(it.name, fontWeight = FontWeight.Bold)
                    Text(
                        it.url,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .clickable { openUri(Uri.parse(it.url)) }
                    )
                    Text(it.text, fontSize = 11.sp)
                }
        }
    }
}
