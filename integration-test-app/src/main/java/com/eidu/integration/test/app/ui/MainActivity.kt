package com.eidu.integration.test.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eidu.integration.test.app.R
import com.eidu.integration.test.app.model.LearningApp
import com.eidu.integration.test.app.ui.screens.EditLearningAppScreen
import com.eidu.integration.test.app.ui.screens.LearningAppResultScreen
import com.eidu.integration.test.app.ui.screens.LearningAppsScreen
import com.eidu.integration.test.app.ui.screens.LearningUnitsScreen
import com.eidu.integration.test.app.ui.screens.licenses.LicensesScreen
import com.eidu.integration.test.app.ui.screens.licenses.LicenseScreen
import com.eidu.integration.test.app.ui.theme.EIDUIntegrationTestAppTheme
import com.eidu.integration.test.app.ui.viewmodel.LearningAppViewModel
import com.eidu.integration.test.app.ui.viewmodel.LicensesViewModel
import com.eidu.integration.test.app.ui.viewmodel.Result
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val learningAppViewModel: LearningAppViewModel by viewModels()
    private val licensesViewModel: LicensesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_EIDUIntegrationTestApp)

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

        receiveUnitLaunchRequest(intent)

        setContent {
            val navController = rememberNavController()

            val requestedUnitLaunch by learningAppViewModel.requestedUnitLaunch.observeAsState()
            LaunchRequestedUnit(requestedUnitLaunch, learningAppLauncher, navController)

            val goBack: () -> Unit = { navController.navigateUp() }
            EIDUIntegrationTestAppTheme {
                Surface(color = MaterialTheme.colors.background) {
                    NavHost(navController = navController, startDestination = "learning-apps") {
                        composable("learning-apps") {
                            val learningApps =
                                learningAppViewModel.getLearningApps().observeAsState(listOf())

                            LearningAppsScreen(
                                learningApps.value,
                                learningAppViewModel.importStatus,
                                learningAppViewModel::dismissStatus,
                                navigateToUnits = { learningApp -> navController.navigate("learning-apps/${learningApp.packageName}/units") },
                                deleteLearningApp = { learningApp -> learningAppViewModel.deleteLearningApp(learningApp) },
                                editLearningApp = { learningApp -> navController.navigate("learning-apps/${learningApp.packageName}/edit") },
                                openFilePicker = { packageFilePicker.launch(arrayOf("application/zip")) },
                                addLearningApp = { navController.navigate("learning-apps/create") },
                                navigateToOpenSourceLicenses = { navController.navigate("licenses") }
                            )
                        }
                        composable("learning-apps/{app}/units") { backStackEntry ->
                            when (val app: Result<LearningApp> = getAppNameState(backStackEntry)) {
                                is Result.Loading -> CircularProgressIndicator()
                                is Result.Success -> {
                                    val learningApp = app.result
                                    val unitLoadingState by remember {
                                        learningAppViewModel
                                            .getLearningUnitsByPackageName(learningApp.packageName)
                                            .apply {
                                                observe(this@MainActivity) {
                                                    if (it is Result.Error)
                                                        clipboardService.setPrimaryClip(
                                                            ClipData.newPlainText("Error", it.reason)
                                                        )
                                                }
                                            }
                                    }.observeAsState(initial = Result.Loading)

                                    LearningUnitsScreen(
                                        learningApp = learningApp,
                                        learningUnits = unitLoadingState,
                                        { unit -> learningAppViewModel.getUnitIcon(learningApp.packageName, unit) },
                                        { unit ->
                                            learningAppViewModel.launchLearningAppUnit(
                                                learningApp,
                                                unit,
                                                learningAppLauncher,
                                                navController
                                            )
                                        },
                                        goBack
                                    )
                                }
                                is Result.NotFound -> navController.navigate("learning-apps")
                                is Result.Error -> {}
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
                                            navController.navigate("learning-apps/${app.result.packageName}/edit")
                                        },
                                        goBack
                                    )
                                }
                                is Result.Error -> {}
                            }
                        }
                        composable("learning-apps/create") {
                            EditLearningAppScreen(
                                learningApp = null,
                                onSubmit = { updatedApp: LearningApp ->
                                    learningAppViewModel.putLearningApp(
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
                                            learningAppViewModel.putLearningApp(
                                                updatedApp
                                            )
                                        },
                                        goBack
                                    )
                                }
                                is Result.Error -> {}
                            }
                        }

                        composable("licenses") { LicensesScreen(goBack, navController, licensesViewModel) }

                        composable(
                            "licenses/{index}",
                            arguments = listOf(navArgument("index") { type = NavType.IntType })
                        ) { backStackEntry ->
                            LicenseScreen(backStackEntry, goBack, licensesViewModel) {
                                startActivity(Intent(Intent.ACTION_VIEW, it))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LaunchRequestedUnit(
        requestedUnitLaunch: String?,
        learningAppLauncher: ActivityResultLauncher<Intent>,
        navController: NavHostController
    ) {
        LaunchedEffect(requestedUnitLaunch) {
            requestedUnitLaunch?.let {
                learningAppViewModel.launchLearningAppUnit(
                    it,
                    learningAppLauncher,
                    navController
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        receiveUnitLaunchRequest(intent)
    }

    private fun receiveUnitLaunchRequest(intent: Intent) {
        val uri = intent.data
        val path = uri?.path
        learningAppViewModel.requestedUnitLaunch.value =
            if (path != null && uri.authority == "launch-unit")
                path.trim('/').split(':', limit = 2)[1]
            else
                null
    }

    private fun handleLearningAppResult(activityResult: ActivityResult) {
        learningAppViewModel.processUnitRunResult(activityResult)
    }

    private fun handleFilePicked(uri: Uri?) {
        if (uri != null)
            learningAppViewModel.handleLearningPackageFile(uri)
    }

    @Composable
    private fun getAppNameState(backStackEntry: NavBackStackEntry): Result<LearningApp> {
        val appName = backStackEntry.arguments?.getString("app")
            ?: error("No app name specified")
        val app: Result<LearningApp> by remember {
            learningAppViewModel.getLearningAppByPackageName(appName)
        }.observeAsState(initial = Result.Loading)
        return app
    }
}
