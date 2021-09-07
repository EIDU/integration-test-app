package com.eidu.custom_ktlint_rules

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.test.lint
import org.assertj.core.api.Assertions
import org.junit.Test

class NoNotNullAssertionOperatorRuleTest {
    @Test
    fun noAndroidSyntheticImportRule() {
        Assertions.assertThat(
            NoNotNullAssertionOperatorRule().lint(
                """
        fun test(): Int =
            "0".toIntOrNull()!! + 1
        """.trimIndent()
            )
        ).containsExactly(
            LintError(
                2, 22, "no-not-null-assertion-operator",
                "We have decided not to use the !! operator any more. " +
                    "Please use one of the alternatives (e.g. requireNotNull()) and a meaningful error message"
            )
        )
    }
}
