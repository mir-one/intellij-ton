package org.ton.intellij.tolk.psi.impl

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findFile
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.tree.IElementType
import org.ton.intellij.tolk.psi.TolkFile
import org.ton.intellij.tolk.psi.TolkIncludeDefinition
import org.ton.intellij.tolk.psi.impl.TolkIncludeDefinitionMixin.Companion.resolveTolkImport
import org.ton.intellij.tolk.sdk.TolkSdkManager
import org.ton.intellij.tolk.stub.TolkIncludeDefinitionStub

abstract class TolkIncludeDefinitionMixin : StubBasedPsiElementBase<TolkIncludeDefinitionStub>, TolkIncludeDefinition {
    constructor(stub: TolkIncludeDefinitionStub, type: IStubElementType<*, *>) : super(stub, type)
    constructor(node: ASTNode) : super(node)
    constructor(stub: TolkIncludeDefinitionStub?, type: IElementType?, node: ASTNode?) : super(stub, type, node)

    override fun getTextOffset(): Int {
        val stringLiteral = stringLiteral
        return if (stringLiteral != null) {
            stringLiteral.startOffsetInParent + (stringLiteral.rawString?.startOffsetInParent
                ?: return super.getTextOffset())
        } else {
            super.getTextOffset()
        }
    }

    companion object {
        fun resolveTolkImport(project: Project, file: TolkFile, path: String): VirtualFile? {
            var path = path
            if (!path.endsWith(".tolk")) {
                path = "$path.tolk"
            }
            if (path.isEmpty()) return null
            return if (path.startsWith("@stdlib/")) {
                val resolve = TolkSdkManager[project].getSdkRef().resolve(project) ?: return null
                val subPath = path.substringAfter("@stdlib/")
                resolve.stdlibFile.findFile(subPath)
            } else {
                file.virtualFile?.findFileByRelativePath("../$path")
            }
        }
    }
}

val TolkIncludeDefinition.path: String
    get() = stub?.path ?: stringLiteral?.rawString?.text ?: ""

fun TolkIncludeDefinition.resolveFile(project: Project) = resolveTolkImport(project, containingFile as TolkFile, path)