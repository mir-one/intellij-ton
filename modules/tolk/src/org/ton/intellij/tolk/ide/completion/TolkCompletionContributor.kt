package org.ton.intellij.tolk.ide.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.completion.impl.CompletionSorterImpl
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementWeigher
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns.or
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import org.ton.intellij.tolk.psi.*

class TolkCompletionContributor : CompletionContributor() {
    init {
        extend(TolkMacroCompletionProvider)
        extend(TolkTypeCompletionProvider)
        extend(
            CompletionType.BASIC,
            inBlock(),
            TolkKeywordCompletionProvider(
                KEYWORD_PRIORITY,
                "var",
                "val",
                "if",
                "return",
                "repeat",
                "do",
                "while",
                "try",
                "assert",
                "throw"
            )
        )
        extend(TolkFunCompletionProvider)
        extend(TolkParameterCompletionProvider)
        extend(
            CompletionType.BASIC,
            psiElement().withParent(
                psiElement().withElementType(TokenType.ERROR_ELEMENT).withParent(TolkFile::class.java)
            ),
            TolkKeywordCompletionProvider(
                CONTEXT_KEYWORD_PRIORITY,
                "fun",
                "const",
                "global",
                "get fun",
                "tolk",
                "type",
                "struct",
            )
        )
        extend(
            CompletionType.BASIC,
            psiElement().afterLeaf(
                psiElement(TolkElementTypes.RBRACE).withAncestor(
                    2,
                    psiElement(TolkElementTypes.IF_STATEMENT)
                )
            ),
            TolkKeywordCompletionProvider(
                CONTEXT_KEYWORD_PRIORITY,
                "else",
            )
        )
        extend(
            CompletionType.BASIC,
            psiElement().afterLeaf(
                psiElement(TolkElementTypes.RBRACE).withAncestor(
                    2,
                    psiElement(TolkElementTypes.DO_STATEMENT)
                )
            ),
            TolkKeywordCompletionProvider(
                CONTEXT_KEYWORD_PRIORITY,
                "else",
            )
        )
        extend(TolkCommonCompletionProvider)
        extend(TolkDotExpressionCompletionProvider)
        extend(TolkExpressionFieldProvider)
        extend(TolkMatchPatternTypesCompletionProvider)
        extend(TolkAnnotationCompletionProvider)
    }

    fun extend(provider: TolkCompletionProvider) {
        extend(CompletionType.BASIC, provider.elementPattern, provider)
    }

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(parameters, withTolkSorter(parameters, result))
    }

    private fun inBlock() =
        psiElement().withParent(
            psiElement(TolkReferenceExpression::class.java).withParent(
                psiElement(
                    TolkExpressionStatement::class.java
                ).inside(TolkBlockStatement::class.java)
            )
        ).andNot(
            psiElement().afterLeaf(
                psiElement().withText(StandardPatterns.string().matches("\\d+"))
            )
        )

    private fun baseFunctionAttributePattern(
        vararg afterLeafs: ElementPattern<out PsiElement>,
    ) = psiElement()
        .withAncestor(2, psiElement(TolkFunction::class.java))
        .afterLeaf(
            or(
                *afterLeafs
            )
        ).andNot(
            psiElement().beforeLeaf(psiElement(TolkElementTypes.IDENTIFIER))
        )

    companion object {
        const val KEYWORD_PRIORITY = 20.0
        const val CONTEXT_KEYWORD_PRIORITY = 25.0
        const val NOT_IMPORTED_FUNCTION_PRIORITY = 3.0
        const val FUNCTION_PRIORITY = NOT_IMPORTED_FUNCTION_PRIORITY + 10.0
        const val NOT_IMPORTED_VAR_PRIORITY = 5.0
        const val VAR_PRIORITY = NOT_IMPORTED_VAR_PRIORITY + 10.0

        private val TOLK_COMPLETION_WEIGHERS_GROUPED: List<AnchoredWeigherGroup> =
            splitIntoGroups(TolkCompletionWeigher.WEIGHERS)

        fun withTolkSorter(
            parameters: CompletionParameters,
            result: CompletionResultSet
        ): CompletionResultSet {
            var sorter = (CompletionSorter.defaultSorter(parameters, result.prefixMatcher) as CompletionSorterImpl)
                .withoutClassifiers { it.id == "liftShorter" }
            for (weigher in TOLK_COMPLETION_WEIGHERS_GROUPED) {
                sorter = sorter.weighAfter(weigher.anchor, *weigher.weighers)
            }
            return result.withRelevanceSorter(sorter)
        }

        private fun splitIntoGroups(weighersWithAnchors: List<Any>): List<AnchoredWeigherGroup> {
            val firstEntry = weighersWithAnchors.firstOrNull() ?: return emptyList()
            check(firstEntry is String) {
                "The first element in the weigher list must be a string placeholder like \"priority\"; " +
                        "actually it is `${firstEntry}`"
            }
            val result = mutableListOf<AnchoredWeigherGroup>()
            val weigherIds = hashSetOf<String>()
            var currentAnchor: String = firstEntry
            var currentWeighers = mutableListOf<LookupElementWeigher>()
            // Add "dummy weigher" in order to execute `is String ->` arm in the last iteration
            for (weigherOrAnchor in weighersWithAnchors.asSequence().drop(1).plus(sequenceOf("dummy weigher"))) {
                when (weigherOrAnchor) {
                    is String -> {
                        if (currentWeighers.isNotEmpty()) {
                            result += AnchoredWeigherGroup(currentAnchor, currentWeighers.toTypedArray())
                            currentWeighers = mutableListOf()
                        }
                        currentAnchor = weigherOrAnchor
                    }

                    is TolkCompletionWeigher -> {
                        if (!weigherIds.add(weigherOrAnchor.id)) {
                            error(
                                "Found a ${TolkCompletionWeigher::class.simpleName}.id duplicate: " +
                                        "`${weigherOrAnchor.id}`"
                            )
                        }
                        currentWeighers += RsCompletionWeigherAsLookupElementWeigher(weigherOrAnchor)
                    }

                    else -> error(
                        "The weigher list must consists of String placeholders and instances of " +
                                "${TolkCompletionWeigher::class.simpleName}, got ${weigherOrAnchor.javaClass}"
                    )
                }
            }
            return result
        }

        private class AnchoredWeigherGroup(val anchor: String, val weighers: Array<LookupElementWeigher>)

        private class RsCompletionWeigherAsLookupElementWeigher(
            private val weigher: TolkCompletionWeigher
        ) : LookupElementWeigher(weigher.id, /* negated = */ false, /* dependsOnPrefix = */ false) {
            override fun weigh(element: LookupElement): Comparable<*> {
                val rsElement = element.`as`(TolkLookupElement::class.java)
                return weigher.weigh(rsElement ?: element)
            }
        }
    }
}
