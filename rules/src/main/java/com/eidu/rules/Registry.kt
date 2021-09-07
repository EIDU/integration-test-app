@file:Suppress("UnstableApiUsage")

package com.eidu.rules

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.eidu.rules.detectors.AndroidXFragmentDetector

class Registry : IssueRegistry() {
    override val vendor = Vendor("EIDU")
    override val issues get() = listOf(AndroidXFragmentDetector.ISSUE)
}
