package com.eidu.custom_ktlint_rules

import com.pinterest.ktlint.core.Rule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes

class NoAndroidSyntheticImportRule : Rule("no-android-synthetic-import") {
    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        if (node.elementType == KtStubElementTypes.IMPORT_DIRECTIVE) {
            val importDirective = node.psi as KtImportDirective
            val path = importDirective.importPath?.pathStr
            if (path != null && path.contains("kotlinx.android.synthetic")) {
                emit(node.startOffset, "Importing from the Android Synthetic", false)
            }
        }
    }
}
