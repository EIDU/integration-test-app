@file:Suppress("UnstableApiUsage")

package com.eidu.rules.detectors

import com.android.SdkConstants.CLASS_V4_FRAGMENT
import com.android.tools.lint.checks.FragmentDetector
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.TextFormat
import org.jetbrains.uast.UClass

class AndroidXFragmentDetector : Detector(), SourceCodeScanner {
    private val detector = FragmentDetector()

    companion object {
        /** Are fragment (including androidx.fragment.app.Fragment) subclasses instantiatable? */
        @JvmField
        val ISSUE = Issue.create(
            id = "AndroidXValidFragment",
            briefDescription = FragmentDetector.ISSUE.getBriefDescription(TextFormat.RAW),
            explanation = FragmentDetector.ISSUE.getExplanation(TextFormat.RAW),
            category = FragmentDetector.ISSUE.category,
            androidSpecific = FragmentDetector.ISSUE.isAndroidSpecific(),
            priority = FragmentDetector.ISSUE.priority,
            severity = FragmentDetector.ISSUE.defaultSeverity,
            moreInfo = "https://issuetracker.google.com/issues/119675579",
            implementation = Implementation(AndroidXFragmentDetector::class.java, Scope.JAVA_FILE_SCOPE)
        )
    }

    override fun applicableSuperClasses(): List<String> =  // include androidx.fragment.app.Fragment
        detector.applicableSuperClasses() + CLASS_V4_FRAGMENT.newName()

    override fun visitClass(context: JavaContext, declaration: UClass) = detector.visitClass(context, declaration)
}
