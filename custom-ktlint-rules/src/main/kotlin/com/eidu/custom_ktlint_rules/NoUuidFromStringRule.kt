package com.eidu.custom_ktlint_rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CALLABLE_REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_DIRECTIVE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

private const val ERROR_MESSAGE =
    "Do not use UUID::fromString as its usages are not checked for null-safety; " +
    "use String::toUUID (declared in com.eidu.util.UuidHelper) instead. " +
    "More details here: https://app.asana.com/0/1151096659893849/1198001158386536/f"

class NoUuidFromStringRule : Rule("no-uuid-from-string") {
    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        when (node.elementType) {
            DOT_QUALIFIED_EXPRESSION ->
                if (node.psi.text.startsWith("UUID.fromString"))
                    emit(node.startOffset, ERROR_MESSAGE, false)
            CALLABLE_REFERENCE_EXPRESSION ->
                if (node.psi.text.startsWith("UUID::fromString"))
                    emit(node.startOffset, ERROR_MESSAGE, false)
            IMPORT_DIRECTIVE ->
                if (node.psi.text.startsWith("import java.util.UUID.fromString"))
                    emit(node.startOffset, ERROR_MESSAGE, false)
        }
    }
}
