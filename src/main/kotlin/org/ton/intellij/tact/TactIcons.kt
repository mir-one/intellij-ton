package org.ton.intellij.tact

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader

object TactIcons {
    val FILE = IconLoader.getIcon("icons/tact.svg", TactIcons::class.java)
    val FUNCTION = AllIcons.Nodes.Function
    val PARAMETER = AllIcons.Nodes.Parameter
    val CONSTANT = AllIcons.Nodes.Constant
    val VARIABLE = AllIcons.Nodes.Variable
    val GLOBAL_VARIABLE = AllIcons.Nodes.Gvariable
    val RECURSIVE_CALL = AllIcons.Gutter.RecursiveMethod
    val IMPLEMENTED_METHOD = AllIcons.Gutter.ImplementedMethod
    val IMPLEMENTING_METHOD = AllIcons.Gutter.ImplementingMethod
    val OVERRIDING_METHOD = AllIcons.Gutter.OverridingMethod
    val CONTRACT = AllIcons.Nodes.Controller
    val TRAIT = AllIcons.Nodes.Interface
    val STRUCT = AllIcons.Nodes.Record
    val MESSAGE = AllIcons.Nodes.Record
    val PRIMITIVE = AllIcons.Nodes.Type
}
