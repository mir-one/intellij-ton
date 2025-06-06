package org.ton.intellij.tolk.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.search.SearchScope
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.util.PsiTreeUtil
import org.ton.intellij.tolk.TolkIcons
import org.ton.intellij.tolk.psi.TolkAnnotation
import org.ton.intellij.tolk.psi.TolkStruct
import org.ton.intellij.tolk.psi.TolkStructField
import org.ton.intellij.tolk.stub.TolkStructStub
import org.ton.intellij.tolk.type.TolkStructTy
import org.ton.intellij.tolk.type.TolkTy
import org.ton.intellij.util.greenStub
import javax.swing.Icon

abstract class TolkStructMixin : TolkNamedElementImpl<TolkStructStub>, TolkStruct {
    constructor(node: ASTNode) : super(node)

    constructor(stub: TolkStructStub, stubType: IStubElementType<*, *>) : super(stub, stubType)

    override fun getUseScope(): SearchScope {
        return super.getUseScope()
    }

    override val type: TolkStructTy get() = declaredType

    override fun getBaseIcon() = TolkIcons.STRUCTURE

    override fun getIcon(flags: Int): Icon? = getBaseIcon()

    override fun isEquivalentTo(another: PsiElement?): Boolean {
        if (this === another) return true
        if (another !is TolkStruct) return false
        if (name != another.name) return false
        if (originalElement == another.originalElement) return true
        val thisFile = containingFile.originalFile
        val anotherFile = another.containingFile.originalFile
        return thisFile == anotherFile
    }
}

val TolkStruct.declaredType: TolkStructTy get() = TolkTy.struct(this)

val TolkStruct?.structFields: List<TolkStructField> get() {
    // todo: optimize by stub
    return this?.structBody?.structFieldList ?: emptyList()
}

val TolkStruct.annotationList
    get() = PsiTreeUtil.getChildrenOfTypeAsList(this, TolkAnnotation::class.java)

val TolkStruct.isDeprecated: Boolean
    get() = greenStub?.isDeprecated ?: annotationList.any { it.identifier?.textMatches("deprecated") == true }
