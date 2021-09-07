package utils

import org.gradle.api.Project

fun Project.getAppVersion(): String {
    val currentCommitTag = getCurrentCommitVersionTag()

    if (currentCommitTag != null)
        return tagToVersionName(currentCommitTag)
    else {
        val currentBranch = getGitBranch()
        val mostRecentTag = getMostRecentVersionTag()
        if (mostRecentTag == "") {
            return "0.0.1"
        }
        val mostRecentVersionArray = tagToVersionArray(mostRecentTag).toMutableList()

        when {
            currentBranch == "main" -> {
                mostRecentVersionArray[1]++
                mostRecentVersionArray[2] = 0
                return versionArrayToVersionName(mostRecentVersionArray) + "-snapshot"
            }
            currentBranch.matches("patch-.*\\..*".toRegex()) -> {
                val patchVersionArray = tagToVersionArray(currentBranch)
                val patchedVersion = "v-${patchVersionArray[0]}.${patchVersionArray[1]}"

                val highestRelevantTag = getMostRelevantVersionTag(patchedVersion)
                if (highestRelevantTag == "") {
                    return tagToVersionName(patchedVersion) + ".1"
                }

                val highestRelevantVersionArray = tagToVersionArray(highestRelevantTag).toMutableList()
                highestRelevantVersionArray[2] ++
                return versionArrayToVersionName(highestRelevantVersionArray)
            }
            else -> {
                return tagToVersionName(mostRecentTag) + "-" + currentBranch.replace(" ", "")
            }
        }
    }
}

fun Project.getAppVersionUserFacingName(): String {
    val versionName = getAppVersion()
    val version = versionNameToVersionArray(versionName)
    val suffix = versionNameSuffix(versionName)
    return "${version[0] * 100 + version[1]}" +
        if (version[2] > 0) ".${version[2]}" else "" +
        if (suffix != "") "-${suffix}" else ""
}

fun String.toVersionCode(): Int {
    val parts = split('-')[0].split('.')
    return (parts[0].toInt() * 1000 + parts[1].toInt()) * 1000 + parts[2].toInt()
}

private fun tagToVersionName(versionTag: String): String = versionTag.split('-')[1]

private fun tagToVersionArray(versionTag: String): List<Int> =
    tagToVersionName(versionTag).split('.').map { it.toInt() }

private fun versionArrayToVersionName(versionArray: List<Int>): String = versionArray.joinToString(".")

private fun versionNameToVersionArray(versionName: String): List<Int> =
    versionName.split('-')[0].split('.').map { it.toInt() }

private fun versionNameSuffix(versionName: String): String {
    val parts = versionName.split('-')
    return if (parts.count() > 1) parts[1] else ""
}
