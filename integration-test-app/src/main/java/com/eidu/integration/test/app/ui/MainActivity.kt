package com.eidu.integration.test.app.ui

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
import com.eidu.integration.test.app.model.LearningApp
import com.eidu.integration.test.app.ui.screens.EditLearningAppScreen
import com.eidu.integration.test.app.ui.screens.LearningAppResultScreen
import com.eidu.integration.test.app.ui.screens.LearningAppsScreen
import com.eidu.integration.test.app.ui.screens.LearningUnitsScreen
import com.eidu.integration.test.app.ui.theme.EIDUIntegrationTestAppTheme
import com.eidu.integration.test.app.ui.viewmodel.LearningAppViewModel
import com.eidu.integration.test.app.ui.viewmodel.Result
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val learningAppViewModel: LearningAppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val learningAppLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ::handleLearningAppResult
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
            EIDUIntegrationTestAppTheme {
                Surface(color = MaterialTheme.colors.background) {
                    NavHost(navController = navController, startDestination = "learning-apps") {
                        composable("learning-apps") {
                            val learningApps =
                                learningAppViewModel.getLearningApps().observeAsState(listOf())

                            LearningAppsScreen(
                                learningApps.value,
                                { learningApp -> navController.navigate("learning-apps/${learningApp.name}/units") },
                                { learningApp -> learningAppViewModel.deleteLearningApp(learningApp) },
                                { learningApp -> navController.navigate("learning-apps/${learningApp.name}/edit") },
                                { packageFilePicker.launch(arrayOf("application/zip")) },
                                { navController.navigate("learning-apps/create") }
                            )
                        }
                        composable("learning-apps/{app}/units") { backStackEntry ->
                            when (val app: Result<LearningApp> = getAppNameState(backStackEntry)) {
                                is Result.Loading -> CircularProgressIndicator()
                                is Result.Success -> {
                                    val learningApp = app.result
                                    val unitLoadingState by remember {
                                        learningAppViewModel
                                            .loadUnitsFromLearningPackageUnitsFile(
                                                learningApp,
                                                clipboardService
                                            )
                                    }.observeAsState(initial = Result.Loading)

                                    LearningUnitsScreen(
                                        learningApp = learningApp,
                                        learningUnits = unitLoadingState,
                                        { unit ->
                                            learningAppViewModel.launchLearningAppUnit(
                                                applicationContext,
                                                learningApp,
                                                unit,
                                                learningAppLauncher,
                                                navController
                                            )
                                        },
                                        {
                                            navController.navigate("learning-apps/${learningApp.name}/edit")
                                        },
                                        goBack
                                    )
                                }
                                is Result.NotFound -> navController.navigate("learning-apps")
                            }
                        }
                        composable("learning-apps/{app}/result") { backStackEntry ->
                            when (val app: Result<LearningApp> = getAppNameState(backStackEntry)) {
                                is Result.Loading -> CircularProgressIndicator()
                                is Result.NotFound -> navController.navigate("learning-apps")
                                is Result.Success -> {
                                    val appResult = learningAppViewModel.getLearningAppResult()
                                        .observeAsState(initial = Result.Loading)
                                    LearningAppResultScreen(
                                        learningApp = app.result,
                                        learningAppResult = appResult.value,
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
                                            navController.navigate("learning-apps/${app.result.name}/edit")
                                        },
                                        goBack
                                    )
                                }
                            }
                        }
                        composable("learning-apps/create") {
                            EditLearningAppScreen(
                                learningApp = null,
                                onSubmit = { updatedApp: LearningApp ->
                                    learningAppViewModel.upsertLearningApp(
                                        updatedApp
                                    )
                                },
                                goBack
                            )
                        }
                        composable("learning-apps/{app}/edit") { backStackEntry ->
                            when (val app: Result<LearningApp> = getAppNameState(backStackEntry)) {
                                is Result.Loading -> CircularProgressIndicator()
                                is Result.NotFound -> navController.navigate("learning-apps")
                                is Result.Success -> {
                                    EditLearningAppScreen(
                                        learningApp = app.result,
                                        onSubmit = { updatedApp: LearningApp ->
                                            learningAppViewModel.upsertLearningApp(
                                                updatedApp
                                            )
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

    private fun handleLearningAppResult(activityResult: ActivityResult) {
        learningAppViewModel.processUnitRunResult(activityResult)
    }

    private fun handleFilePicked(uri: Uri?) {
        if (uri != null) {
            learningAppViewModel.handleLearningPackageFile(uri)
        }
    }

    @Composable
    private fun getAppNameState(backStackEntry: NavBackStackEntry): Result<LearningApp> {
        val appName = backStackEntry.arguments?.getString("app")
            ?: error("No app name specified")
        val app: Result<LearningApp> by remember {
            learningAppViewModel.getLearningAppByName(appName)
        }.observeAsState(initial = Result.Loading)
        return app
    }
}
