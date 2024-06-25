package licenses

import com.github.jk1.license.LicenseReportExtension
import com.github.jk1.license.ModuleData
import com.github.jk1.license.ProjectData
import com.github.jk1.license.render.ReportRenderer
import utils.PersistentCache
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class FullJsonReportRenderer : ReportRenderer {
    private val httpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

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
    ) {
        init {
            require(licenses.isNotEmpty()) { "Dependency $moduleGroup:$moduleName:$version has no licenses" }
        }
    }

    @Serializable
    data class License(val name: String, val url: String, val text: String)

    private lateinit var persistentCache: PersistentCache
    private val cachedLicenseTexts = mutableMapOf<String, String>()

    override fun render(data: ProjectData) {
        persistentCache = PersistentCache(data.project)

        File(LicenseReportExtension(data.project).absoluteOutputDir, "dependencies.json")
            .writeText(
                Json.encodeToString(
                    Dependencies(
                        data.allDependencies.map { dependency ->
                            Dependency(
                                dependency.poms.first().name,
                                dependency.poms.first().description,
                                dependency.group,
                                dependency.name,
                                dependency.version,
                                dependency.poms.first().projectUrl,
                                dependency.poms.flatMap { it.licenses }
                                    .filter { it.name !in LICENSES_TO_IGNORE }
                                    .map {
                                        License(
                                            it.name,
                                            it.url,
                                            licenseText(dependency, it.url)
                                        )
                                    }
                            )
                        }
                    )
                )
            )
    }

    private fun licenseText(moduleData: ModuleData, licenseUrl: String): String {
        val bestUrl = PLAIN_LICENSE_TEXT_URLS[licenseUrl] ?: licenseUrl
        return staticLicenseText(bestUrl) ?: cachedLicenseText(bestUrl) ?: persistentlyCachedLicenseText(bestUrl) ?: fetchLicenseText(moduleData, bestUrl)
    }

    private fun persistentlyCachedLicenseText(bestUrl: String): String? = persistentCache.get(bestUrl)

    private fun staticLicenseText(url: String) = STATIC_LICENSE_TEXTS_BY_URL[url]

    private fun cachedLicenseText(licenseUrl: String): String? = cachedLicenseTexts[licenseUrl]

    private fun fetchLicenseText(moduleData: ModuleData, licenseUrl: String): String {
        val response = httpClient.send(
            HttpRequest.newBuilder(URI.create(licenseUrl)).build(), HttpResponse.BodyHandlers.ofString()
        )
        check(response.statusCode() in 200..299) {
            "Got HTTP status ${response.statusCode()} when fetching license text for ${moduleData.coordinates} from $licenseUrl"
        }
        check(response.headers().allValues("Content-Type").any { it.startsWith("text/plain") }) {
            "Got non-plaintext License text for ${moduleData.coordinates} from $licenseUrl"
        }

        return response.body().also { cacheLicenseText(licenseUrl, it) }
    }

    private fun cacheLicenseText(url: String, text: String) {
        cachedLicenseTexts[url] = text
        persistentCache.put(url, text)
    }

    private val ModuleData.coordinates get() = "$group:$name:$version"
}
