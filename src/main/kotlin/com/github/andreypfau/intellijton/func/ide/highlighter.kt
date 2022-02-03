package com.github.andreypfau.intellijton.func.ide

import com.github.andreypfau.intellijton.func.parser.FuncLexerAdapter
import com.github.andreypfau.intellijton.func.psi.FuncTypes.*
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType

class FuncSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?) = FuncSyntaxHighlighter
}

object FuncSyntaxHighlighter : SyntaxHighlighterBase() {
    private val tokenMapping = mapOf(
        COMMENT to FuncColor.COMMENT,
        LBRACE to FuncColor.BRACES,
        RBRACE to FuncColor.BRACES,
        LBRACKET to FuncColor.BRACKETS,
        RBRACKET to FuncColor.BRACKETS,
        LPAREN to FuncColor.PARENTHESES,
        RPAREN to FuncColor.PARENTHESES,
        SEMICOLON to FuncColor.SEMICOLON,
        FUNCTION_IDENTIFIER to FuncColor.FUNCTION_IDENTIFIER,

        SCIENTIFICNUMBER to FuncColor.NUMBER,
        NUMBER_LITERAL to FuncColor.NUMBER,
        FIXEDNUMBER to FuncColor.NUMBER,
        DECIMALNUMBER to FuncColor.NUMBER,
        HEXNUMBER to FuncColor.NUMBER,

        STRINGLITERAL to FuncColor.STRING,
    )
        .plus(keywords().map { it to FuncColor.KEYWORD })
        .plus(literals().map { it to FuncColor.KEYWORD })
        .plus(operators().map { it to FuncColor.OPERATION_SIGN })
        .mapValues { it.value.textAttributesKey }

    private fun keywords() = setOf(
        IF, IFNOT, ELSE, WHILE, DO, RETURN, VAR,
        INT, CELL, SLICE, BUILDER, TUPLE, CONT,
        IMPURE, INLINE, INLINE_REF, METHOD_ID
    )

    private fun literals() = setOf(BOOLEAN_LITERAL)
    private fun operators() = setOf(
        NOT, ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, MULT_ASSIGN, DIV_ASSIGN, PERCENT_ASSIGN,
        PLUS, MINUS, MULT, DIV, CARET,
        LESS, MORE, LESSEQ, MOREEQ,
        AND, ANDAND, OR, OROR,
        EQ, NEQ, TO,
        INC, DEC,
        TILDE, PERCENT,
        LSHIFT, RSHIFT,
        LEFT_ASSEMBLY, RIGHT_ASSEMBLY
    )

    override fun getHighlightingLexer() = FuncLexerAdapter()
    override fun getTokenHighlights(tokenType: IElementType) = pack(tokenMapping[tokenType])
}