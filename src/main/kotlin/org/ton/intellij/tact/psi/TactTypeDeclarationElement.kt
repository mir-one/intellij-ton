package org.ton.intellij.tact.psi

import org.ton.intellij.tact.type.TactTy

interface TactTypeDeclarationElement : TactNamedElement {
    val declaredTy: TactTy

    val superTraits: Sequence<TactTypeDeclarationElement> get() = emptySequence()

    val members: Sequence<TactNamedElement>
}
