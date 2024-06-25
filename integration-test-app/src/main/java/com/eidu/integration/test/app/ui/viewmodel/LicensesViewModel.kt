package com.eidu.integration.test.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.eidu.content.learningpackages.util.json
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import java.io.InputStreamReader
import javax.inject.Inject

@HiltViewModel
class LicensesViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) : ViewModel() {

    @Serializable
    data class Dependencies(val dependencies: List<Dependency>)

    @Serializable
    data class Dependency(
        val name: String,
        val description: String,
        val moduleGroup: String,
        val moduleName: String,
        val version: String,
        val url: String,
        val licenses: List<License>
    )

    @Serializable
    data class License(val name: String, val url: String, val text: String)

    val dependencies: List<Dependency> by lazy {
        json.decodeFromString<Dependencies>(
            InputStreamReader(applicationContext.assets.open("dependencies.json")).use { it.readText() }
        ).dependencies
    }
}
