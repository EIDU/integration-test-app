package com.eidu.custom_ktlint_rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.EXCLEXCL
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * This rule forbids using the !! (not-null assertion) operator.
 */
class NoNotNullAssertionOperatorRule : Rule("no-not-null-assertion-operator") {
    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        if (node.elementType == EXCLEXCL) emit(
            node.startOffset,
            "We have decided not to use the !! operator any more. " +
                "Please use one of the alternatives (e.g. requireNotNull()) and a meaningful error message",
            false
        )
    }
}
