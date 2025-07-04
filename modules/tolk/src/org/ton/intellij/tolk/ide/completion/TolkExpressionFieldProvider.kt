package org.ton.intellij.tolk.ide.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.ton.intellij.tolk.psi.TolkStructExpression
import org.ton.intellij.tolk.psi.TolkStructExpressionField
import org.ton.intellij.tolk.psi.impl.structFields
import org.ton.intellij.tolk.type.TolkTyStruct

object TolkExpressionFieldProvider : TolkCompletionProvider() {
    override val elementPattern: ElementPattern<out PsiElement> = psiElement()
        .withParent(TolkStructExpressionField::class.java)

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val element = parameters.position
        val parent = element.parent as? TolkStructExpressionField ?: return
        if (parent.identifier != element) return

        val structExpr = parent.parentOfType<TolkStructExpression>() ?: return
        val structTy = structExpr.type?.unwrapTypeAlias() as? TolkTyStruct ?: return
        val ctx = TolkCompletionContext(parent)
        val existedFields = structExpr.structExpressionBody.structExpressionFieldList.mapNotNull {
            if (it == parent) return@mapNotNull null
            it.referenceName
        }

        structTy.psi.structFields.asReversed().forEachIndexed { index, field ->
            if (field.name in existedFields) return@forEachIndexed
            result.addElement(
                PrioritizedLookupElement.withPriority(
                    field.toLookupElementBuilder(ctx)
                        .toTolkLookupElement(TolkLookupElementData(elementKind = TolkLookupElementData.ElementKind.FIELD)),
                    index.toDouble() * 0.001 + 1.0
                )
            )
        }
    }
}
