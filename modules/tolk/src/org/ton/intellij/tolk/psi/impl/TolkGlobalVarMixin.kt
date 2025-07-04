package org.ton.intellij.tolk.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import org.ton.intellij.tolk.TolkIcons
import org.ton.intellij.tolk.doc.psi.TolkDocComment
import org.ton.intellij.tolk.psi.TolkGlobalVar
import org.ton.intellij.tolk.stub.TolkGlobalVarStub
import org.ton.intellij.tolk.type.TolkTy
import org.ton.intellij.util.childOfType
import javax.swing.Icon

abstract class TolkGlobalVarMixin : TolkNamedElementImpl<TolkGlobalVarStub>, TolkGlobalVar {
    constructor(node: ASTNode) : super(node)

    constructor(stub: TolkGlobalVarStub, stubType: IStubElementType<*, *>) : super(stub, stubType)

    override val type: TolkTy? get() = typeExpression?.type

    override val doc: TolkDocComment? get() = childOfType()

    override fun getIcon(flags: Int): Icon? = TolkIcons.GLOBAL_VARIABLE
}
