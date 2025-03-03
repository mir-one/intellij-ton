package org.ton.intellij.func.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import org.ton.intellij.func.FuncIcons
import org.ton.intellij.func.psi.FuncFunctionParameter
import org.ton.intellij.func.stub.FuncFunctionParameterStub
import javax.swing.Icon

abstract class FuncFunctionParameterMixin : FuncNamedElementImpl<FuncFunctionParameterStub>, FuncFunctionParameter {
    constructor(node: ASTNode) : super(node)

    constructor(stub: FuncFunctionParameterStub, stubType: IStubElementType<*, *>) : super(stub, stubType)

    override fun getIcon(flags: Int): Icon = FuncIcons.PARAMETER
}
