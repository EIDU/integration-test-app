package utils

import org.gradle.api.Project
import java.io.File

class PersistentCache(project: Project) {
    private val cacheDir = project.layout.buildDirectory.dir("intermediates/license_texts").get().asFile

    init {
        cacheDir.mkdirs()
    }

    fun put(key: String, value: String) = File(cacheDir, safeKey(key)).writeText(value)

    fun get(key: String): String? {
        val cacheFile = File(cacheDir, safeKey(key))
        return if (cacheFile.exists())
            cacheFile.readText()
        else
            null
    }

    companion object {
        private fun safeKey(key: String) = key.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
    }
}
