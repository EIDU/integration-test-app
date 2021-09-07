package utils

import org.gradle.api.Project

fun Project.getGitHash(): String = run("git rev-parse HEAD")

fun Project.getGitBranch(): String = run("git rev-parse --abbrev-ref HEAD")

fun Project.getCurrentCommitVersionTag(): String? =
    run("git tag --points-at HEAD")
        .split("\n")
        .filter { it.matches("v-.*\\..*\\..*".toRegex()) }
        .takeUnless { it.isEmpty() }
        ?.single()

fun Project.getMostRecentVersionTag(): String = getMostRelevantVersionTag("v-*.*.*")

fun Project.getMostRelevantVersionTag(patchedVersion: String): String =
    run("git tag -l --sort -version:refname $patchedVersion | head -n 1")
