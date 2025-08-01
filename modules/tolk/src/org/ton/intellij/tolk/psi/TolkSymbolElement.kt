package org.ton.intellij.tolk.psi

import org.ton.intellij.tolk.type.TolkTy

interface TolkSymbolElement : TolkNamedElement, TolkTypedElement, TolkTopLevelElement

interface TolkTypeSymbolElement : TolkSymbolElement {
    override val type: TolkTy
}

interface TolkLocalSymbolElement : TolkSymbolElement, TolkNamedElement
