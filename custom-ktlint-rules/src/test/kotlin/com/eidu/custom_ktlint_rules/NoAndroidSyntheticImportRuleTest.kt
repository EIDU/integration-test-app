package com.eidu.custom_ktlint_rules

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NoAndroidSyntheticImportRuleTest {
    @Test
    fun noAndroidSyntheticImportRule() {
        assertThat(
            NoAndroidSyntheticImportRule().lint(
                """
        import a.b.c
        import kotlinx.android.synthetic.main.SuperClass
        """.trimIndent()
            )
        ).containsExactly(
            LintError(
                2, 1, "no-android-synthetic-import",
                "Importing from the Android Synthetic"
            )
        )
    }
}
