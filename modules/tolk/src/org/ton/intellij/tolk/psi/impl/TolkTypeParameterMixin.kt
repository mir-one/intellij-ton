package org.ton.intellij.tolk.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import org.ton.intellij.tolk.TolkIcons
import org.ton.intellij.tolk.psi.TolkTypeParameter
import org.ton.intellij.tolk.stub.TolkTypeParameterStub
import org.ton.intellij.tolk.type.TolkTy
import org.ton.intellij.tolk.type.TolkTypeParameterTy
import javax.swing.Icon

abstract class TolkTypeParameterMixin : TolkNamedElementImpl<TolkTypeParameterStub>, TolkTypeParameter {
    constructor(node: ASTNode) : super(node)

    constructor(stub: TolkTypeParameterStub, stubType: IStubElementType<*, *>) : super(stub, stubType)

    override val type: TolkTy get() = TolkTypeParameterTy.create(this)

    override fun getIcon(flags: Int): Icon? = TolkIcons.PARAMETER
}
