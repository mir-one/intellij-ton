package org.ton.intellij.tolk.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.ton.intellij.tolk.psi.TolkDotExpression
import org.ton.intellij.tolk.psi.TolkFieldLookup
import org.ton.intellij.tolk.psi.reference.TolkFieldLookupReference
import org.ton.intellij.tolk.type.TolkTypeParameterTy

abstract class TolkFieldLookupMixin(node: ASTNode) : ASTWrapperPsiElement(node), TolkFieldLookup {
    override val referenceNameElement: PsiElement? get() = identifier ?: integerLiteral

    override fun getReference(): TolkFieldLookupReference? {
        return if (parentDotExpression.expression.type !is TolkTypeParameterTy) {
            TolkFieldLookupReference(this)
        } else {
            null
        }
    }
}

val TolkFieldLookup.parentDotExpression: TolkDotExpression
    get() = parent as TolkDotExpression
