/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.insight

import com.demonwav.mcdev.facet.MinecraftFacet
import com.demonwav.mcdev.util.findModule
import com.demonwav.mcdev.util.runWriteAction
import com.intellij.psi.JVMElementFactories
import java.awt.Color
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.UReferenceExpression
import org.jetbrains.uast.generate.generationPlugin
import org.jetbrains.uast.generate.replace

fun <T> UElement.findColor(function: (Map<String, Color>, Map.Entry<String, Color>) -> T): T? {
    val parent = this.uastParent
    val expression = parent as? UReferenceExpression ?: return null
    val type = expression.getExpressionType() ?: return null

    val module = this.sourcePsi?.findModule() ?: return null
    val facet = MinecraftFacet.getInstance(module) ?: return null
    for (abstractModuleType in facet.types) {
        val map = abstractModuleType.classToColorMappings
        for (entry in map.entries) {
            // This is such a hack
            // Okay, type will be the fully-qualified class, but it will exclude the actual enum
            // the expression will be the non-fully-qualified class with the enum
            // So we combine those checks and get this
            val colorClass = entry.key.substringBeforeLast('.')
            val colorName = entry.key.substringAfterLast('.')
            if (colorClass.startsWith(type.canonicalText) && colorName == expression.resolvedName ?: continue) {
                return function(map.filterKeys { key -> key.startsWith(colorClass) }, entry)
            }
        }
    }
    return null
}

fun UElement.setColor(color: String) {
    val sourcePsi = this.sourcePsi ?: return
    sourcePsi.containingFile.runWriteAction {
        val project = sourcePsi.project
        val parent = this.uastParent
        val newColorRef = generationPlugin?.getElementFactory(project)?.createQualifiedReference(color, this)
            ?: return@runWriteAction
        if (this.lang.id == "kotlin") {
            // Kotlin UAST is a bit different, annoying but I couldn't find a better way
            val grandparent = parent?.uastParent
            if (grandparent is UQualifiedReferenceExpression) {
                grandparent.replace(newColorRef)
            } else {
                this.replace(newColorRef)
            }
        } else {
            if (parent is UQualifiedReferenceExpression) {
                parent.replace(newColorRef)
            } else {
                this.replace(newColorRef)
            }
        }
    }
}

fun ULiteralExpression.setColor(value: Int) {
    val sourcePsi = this.sourcePsi ?: return
    sourcePsi.containingFile.runWriteAction {
        JVMElementFactories.requireFactory(sourcePsi.language, sourcePsi.project)
            .createExpressionFromText("0x" + Integer.toHexString(value).toUpperCase(), sourcePsi)
            .let(sourcePsi::replace)
    }
}

fun UCallExpression.setColor(red: Int, green: Int, blue: Int) {
    val sourcePsi = this.sourcePsi ?: return
    sourcePsi.containingFile.runWriteAction {
        val r = this.valueArguments[0]
        val g = this.valueArguments[1]
        val b = this.valueArguments[2]

        val factory = JVMElementFactories.requireFactory(sourcePsi.language, sourcePsi.project)

        val literalExpressionOne = factory.createExpressionFromText(red.toString(), null)
        val literalExpressionTwo = factory.createExpressionFromText(green.toString(), null)
        val literalExpressionThree = factory.createExpressionFromText(blue.toString(), null)

        r.sourcePsi?.replace(literalExpressionOne)
        g.sourcePsi?.replace(literalExpressionTwo)
        b.sourcePsi?.replace(literalExpressionThree)
    }
}
