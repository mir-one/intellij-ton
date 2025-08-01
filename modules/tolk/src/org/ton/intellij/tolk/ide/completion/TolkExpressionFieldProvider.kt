package org.ton.intellij.tolk.ide.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.template.impl.ConstantNode
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.startOffset
import com.intellij.util.ProcessingContext
import org.ton.intellij.tolk.psi.TolkStructExpression
import org.ton.intellij.tolk.psi.TolkStructExpressionField
import org.ton.intellij.tolk.psi.impl.structFields
import org.ton.intellij.tolk.type.TolkBitsNTy
import org.ton.intellij.tolk.type.TolkBytesNTy
import org.ton.intellij.tolk.type.TolkCellTy
import org.ton.intellij.tolk.type.TolkIntTyFamily
import org.ton.intellij.tolk.type.TolkSliceTy
import org.ton.intellij.tolk.type.TolkTy
import org.ton.intellij.tolk.type.TolkTyAddress
import org.ton.intellij.tolk.type.TolkTyBool
import org.ton.intellij.tolk.type.TolkTyBuilder
import org.ton.intellij.tolk.type.TolkTyCoins
import org.ton.intellij.tolk.type.TolkTyNull
import org.ton.intellij.tolk.type.TolkTyStruct
import org.ton.intellij.tolk.type.TolkTyTensor
import org.ton.intellij.tolk.type.TolkTyTypedTuple
import org.ton.intellij.tolk.type.TolkTyUnion
import org.ton.intellij.tolk.type.render

object TolkExpressionFieldProvider : TolkCompletionProvider() {
    override val elementPattern: ElementPattern<out PsiElement> = psiElement()
        .withParent(TolkStructExpressionField::class.java)

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        val element = parameters.position
        val parent = element.parent as? TolkStructExpressionField ?: return
        if (parent.identifier != element) return

        val structExpr = parent.parentOfType<TolkStructExpression>() ?: return

        val originalStructExpr = parameters.originalPosition?.parentOfType<TolkStructExpression>() ?: return
        val document = parameters.editor.document
        val startLine = document.getLineNumber(originalStructExpr.structExpressionBody.startOffset)
        val endLine = document.getLineNumber(originalStructExpr.structExpressionBody.endOffset)
        val singleLine = startLine == endLine
        val comma = if (!singleLine) "," else ""

        val structTy = structExpr.type?.unwrapTypeAlias() as? TolkTyStruct ?: return
        val initedFields = structExpr.structExpressionBody.structExpressionFieldList
        val initedFieldNames = initedFields.mapNotNull {
            if (it == parent) return@mapNotNull null
            it.referenceName
        }

        val ctx = TolkCompletionContext(parent)
        structTy.psi.structFields.asReversed().forEachIndexed { index, field ->
            if (field.name in initedFieldNames) return@forEachIndexed

            result.addElement(
                PrioritizedLookupElement.withPriority(
                    field.toLookupElementBuilder(ctx)
                        .withInsertHandler(
                            TemplateStringInsertHandler(
                                ": \$value$$comma",
                                true,
                                "value" to ConstantNode(typeDefaultValue(field.type))
                            )
                        )
                        .toTolkLookupElement(TolkLookupElementData(elementKind = TolkLookupElementData.ElementKind.FIELD)),
                    index.toDouble() * 0.001 + 1.0
                )
            )
        }
    }

    fun typeDefaultValue(type: TolkTy?): String {
        if (type == null) {
            return "null"
        }

        if (type is TolkTyNull) {
            return "null"
        }

        if (type is TolkTyUnion && type.isNullable()) {
            return "null"
        }

        if (type is TolkTyBool) {
            return "false"
        }

        if (type is TolkTyCoins) {
            return "ton(\"0.1\")"
        }

        if (type is TolkIntTyFamily) {
            return "0"
        }

        if (type is TolkBitsNTy || type is TolkBytesNTy) {
            return "createEmptySlice()"
        }

        if (type is TolkTyAddress) {
            return "address(\"\")"
        }

        if (type is TolkCellTy) {
            return "createEmptyCell()"
        }

        if (type is TolkSliceTy) {
            return "createEmptyCell()"
        }

        if (type is TolkTyBuilder) {
            return "beginCell()"
        }

        if (type is TolkTyStruct) {
            return "${type.render()} {}"
        }

        if (type is TolkTyTypedTuple) {
            return "[${type.elements.joinToString(", ") { this.typeDefaultValue(it) }}]"
        }

        if (type is TolkTyTensor) {
            return "(${type.elements.joinToString(", ") { this.typeDefaultValue(it) }})"
        }

        if (type is TolkTyUnion) {
            return this.typeDefaultValue(type.variants.first())
        }

        return "null"
    }
}
