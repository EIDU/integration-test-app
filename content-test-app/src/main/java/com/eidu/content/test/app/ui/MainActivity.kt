package com.eidu.content.test.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eidu.content.test.app.model.ContentApp
import com.eidu.content.test.app.ui.screens.ContentAppResultScreen
import com.eidu.content.test.app.ui.screens.ContentAppsScreen
import com.eidu.content.test.app.ui.screens.ContentUnitsScreen
import com.eidu.content.test.app.ui.theme.EIDUContentTestAppTheme
import com.eidu.content.test.app.ui.viewmodel.ContentAppViewModel
import com.eidu.content.test.app.ui.viewmodel.Result
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val contentAppViewModel: ContentAppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentAppLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ::handleContentAppResult
        )
        val packageFilePicker = registerForActivityResult(
            ActivityResultContracts.OpenDocument(),
            ::handleFilePicked
        )
        val clipboardService = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val copyToClipboardToast = Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT)
        setContent {
            val navController = rememberNavController()
            val goBack: () -> Unit = { navController.navigateUp() }
            EIDUContentTestAppTheme {
                Surface(color = MaterialTheme.colors.background) {
                    NavHost(navController = navController, startDestination = "content-apps") {
                        composable("content-apps") {
                            val contentApps =
                                contentAppViewModel.getContentApps().observeAsState(listOf())

                            ContentAppsScreen(
                                contentApps.value,
                                { contentApp -> navController.navigate("content-apps/${contentApp.name}/units") },
                                { contentApp -> contentAppViewModel.deleteContentApp(contentApp) },
                                { packageFilePicker.launch(arrayOf("application/zip")) }
                            )
                        }
                        composable("content-apps/{app}/units") { backStackEntry ->
                            when (val app: Result<ContentApp> = getAppNameState(backStackEntry)) {
                                is Result.Loading -> CircularProgressIndicator()
                                is Result.Success -> {
                                    val contentApp = app.result
                                    val unitLoadingState by remember {
                                        contentAppViewModel
                                            .loadUnitsFromContentPackageUnitsFile(
                                                contentApp,
                                                clipboardService
                                            )
                                    }.observeAsState(initial = Result.Loading)

                                    ContentUnitsScreen(
                                        contentApp = contentApp,
                                        contentUnits = unitLoadingState,
                                        { unit ->
                                            contentAppViewModel.launchContentAppUnit(
                                                applicationContext,
                                                contentApp,
                                                unit,
                                                contentAppLauncher,
                                                navController
                                            )
                                        },
                                        {
                                            navController.navigate("content-apps/${contentApp.name}/edit")
                                        },
                                        goBack
                                    )
                                }
                                is Result.NotFound -> navController.navigate("content-apps")
                            }
                        }
                        composable("content-apps/{app}/result") { backStackEntry ->
                            when (val app: Result<ContentApp> = getAppNameState(backStackEntry)) {
                                is Result.Loading -> CircularProgressIndicator()
                                is Result.NotFound -> navController.navigate("content-apps")
                                is Result.Success -> {
                                    val appResult = contentAppViewModel.getContentAppResult()
                                        .observeAsState(initial = Result.Loading)
                                    ContentAppResultScreen(
                                        contentApp = app.result,
                                        contentAppResult = appResult.value,
                                        { label, text ->
                                            clipboardService.setPrimaryClip(
                                                ClipData.newPlainText(
                                                    label,
                                                    text
                                                )
                                            )
                                            copyToClipboardToast.show()
                                        },
                                        {
                                            navController.navigate("content-apps/${app.result.name}/edit")
                                        },
                                        goBack
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleContentAppResult(activityResult: ActivityResult) {
        contentAppViewModel.processUnitRunResult(activityResult)
    }

    private fun handleFilePicked(uri: Uri?) {
        if (uri != null) {
            contentAppViewModel.handleContentPackageFile(uri)
        }
    }

    @Composable
    private fun getAppNameState(backStackEntry: NavBackStackEntry): Result<ContentApp> {
        val appName = backStackEntry.arguments?.getString("app")
            ?: error("No app name specified")
        val app: Result<ContentApp> by remember {
            contentAppViewModel.getContentAppByName(appName)
        }.observeAsState(initial = Result.Loading)
        return app
    }
}
