/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.sponge.color

import com.demonwav.mcdev.insight.ColorLineMarkerProvider
import com.demonwav.mcdev.insight.setColor
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.NavigateAction
import com.intellij.psi.JVMElementFactories
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiEditorUtil
import com.intellij.ui.ColorChooser
import java.awt.Color
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UIdentifier
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.toUElementOfType

class SpongeColorLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val (color, workElement) = element.toUElementOfType<UIdentifier>()?.findColor() ?: return null

        val info = SpongeColorInfo(element, color, workElement)
        NavigateAction.setNavigateAction(info, "Change Color", null)

        return info
    }

    private class SpongeColorInfo(
        element: PsiElement,
        color: Color,
        workElement: UElement
    ) : ColorLineMarkerProvider.ColorInfo(
        element,
        color,
        GutterIconNavigationHandler handler@{ _, _ ->
            if (!element.isWritable) {
                return@handler
            }

            if (JVMElementFactories.getFactory(element.language, element.project) == null) {
                // The setColor methods used here require a JVMElementFactory. Unfortunately the Kotlin plugin does not
                // implement it yet. It is better to not display the color chooser at all than deceiving users after
                // after they chose a color.
                return@handler
            }

            val editor = PsiEditorUtil.findEditor(element) ?: return@handler

            val c = ColorChooser.chooseColor(editor.component, "Choose Color", color, false)
            if (c != null) {
                when (workElement) {
                    is ULiteralExpression -> workElement.setColor(c.rgb and 0xFFFFFF)
                    is UCallExpression -> workElement.setColor(c.red, c.green, c.blue)
                }
            }
        }
    )
}
