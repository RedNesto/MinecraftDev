/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2019 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.sponge.inspection

import com.demonwav.mcdev.platform.sponge.SpongeModuleType
import com.demonwav.mcdev.platform.sponge.reference.SpongePluginIdReferenceResolver
import com.demonwav.mcdev.platform.sponge.reference.SpongePluginIdReferenceResolver.isValidPluginId
import com.demonwav.mcdev.platform.sponge.util.SpongePatterns
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.ui.AnActionButtonRunnable
import com.intellij.ui.ListUtil
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.dialog
import com.siyeh.ig.BaseInspection
import com.siyeh.ig.BaseInspectionVisitor
import com.siyeh.ig.InspectionGadgetsFix
import org.jdom.Element
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.DefaultListModel
import javax.swing.JComponent

class SpongePluginIdUsagesInspection : BaseInspection() {

    private val knownIdsList = mutableListOf<String>()

    // This field is serialized automatically
    @JvmField
    var knownIds: String = ""

    override fun getDisplayName() = "Plugin id usages issues"

    override fun getStaticDescription() = "Checks if used plugin id is known or valid."

    override fun buildErrorString(vararg infos: Any?) =
        if (infos.isEmpty()) "Invalid plugin id" else "Unknown plugin id '${infos[0]}'"

    override fun shouldInspect(file: PsiFile) = SpongeModuleType.isInModule(file)

    override fun buildVisitor(): BaseInspectionVisitor {
        return object : BaseInspectionVisitor() {
            override fun visitLiteralExpression(expression: PsiLiteralExpression) {
                if (!SpongePatterns.PLUGIN_ID_USAGES.accepts(expression)) {
                    return
                }

                val pluginId = expression.text.removeSurrounding("\"")
                if (!isValidPluginId(pluginId)) {
                    registerError(expression, ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
                    return
                }

                if (pluginId in knownIdsList) {
                    return
                }

                val pluginIdDeclaration =
                    SpongePluginIdReferenceResolver.findAttributeValueDeclaringPluginId(pluginId, expression)
                if (pluginIdDeclaration == null) {
                    registerError(expression, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, pluginId)
                }
            }
        }
    }

    override fun buildFixes(vararg infos: Any?): Array<InspectionGadgetsFix> {
        val pluginId = infos.firstOrNull() as? String
            ?: return InspectionGadgetsFix.EMPTY_ARRAY
        return arrayOf(AddToKnownListFix(pluginId))
    }

    override fun createOptionsPanel(): JComponent? {
        val list = JBList(knownIdsList)
        return ToolbarDecorator.createDecorator(list)
            .disableUpDownActions()
            .setAddAction(AnActionButtonRunnable {
                val idField = JBTextField()
                val dialog = dialog("Enter plugin id", idField, focusedComponent = idField) {
                    val pluginId = idField.text
                    if (pluginId == null || !isValidPluginId(pluginId)) {
                        listOf(ValidationInfo("Plugin id is invalid", idField))
                    } else {
                        null
                    }
                }

                idField.addKeyListener(object : KeyAdapter() {
                    override fun keyPressed(e: KeyEvent) {
                        if (e.keyCode == KeyEvent.VK_ENTER) {
                            dialog.clickDefaultButton()
                        }
                    }
                })

                if (!dialog.showAndGet()) {
                    return@AnActionButtonRunnable
                }

                val pluginId = idField.text!!

                val model = list.model as DefaultListModel<String>
                val index = model.indexOf(pluginId)
                if (index < 0) {
                    model.addElement(pluginId)
                    knownIdsList.add(pluginId)
                } else {
                    list.selectedIndex = index
                }
            }).setRemoveAction {
                knownIdsList.remove(list.selectedValue)
                ListUtil.removeSelectedItems(list)
            }.createPanel()
    }

    override fun readSettings(node: Element) {
        super.readSettings(node)
        parseString(knownIds, knownIdsList)
    }

    override fun writeSettings(node: Element) {
        if (knownIdsList.isEmpty()) {
            knownIds = ""
        } else {
            knownIds = formatString(knownIdsList)
        }

        super.writeSettings(node)
    }

    private inner class AddToKnownListFix(private val pluginId: String) : InspectionGadgetsFix() {
        override fun doFix(project: Project?, descriptor: ProblemDescriptor?) {
            knownIdsList += pluginId
        }

        override fun getName(): String = "Add plugin id '${pluginId}' to known ids list"

        override fun getFamilyName(): String = "Add to known plugin ids list"
    }
}
