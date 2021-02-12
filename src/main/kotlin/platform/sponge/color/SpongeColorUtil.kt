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

import com.demonwav.mcdev.facet.MinecraftFacet
import com.demonwav.mcdev.platform.sponge.SpongeModuleType
import com.demonwav.mcdev.util.findModule
import com.intellij.psi.PsiType
import com.intellij.psi.search.GlobalSearchScope
import java.awt.Color
import kotlin.math.round
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression

fun UElement.findColor(): Pair<Color, UElement>? {
    val project = this.sourcePsi?.project ?: return null

    val module = this.sourcePsi?.findModule() ?: return null
    val facet = MinecraftFacet.getInstance(module) ?: return null

    if (!facet.isOfType(SpongeModuleType)) {
        return null
    }

    val methodExpression = uastParent as? UCallExpression ?: return null
    if (methodExpression.resolve()?.containingClass?.qualifiedName != "org.spongepowered.api.util.Color") {
        return null
    }
    val arguments = methodExpression.valueArguments
    val types = arguments.map { it.getExpressionType() }

    return when {
        // Single Integer Argument
        types.size == 1 && types[0] == PsiType.INT ->
            handleSingleArgument(arguments[0])?.let { it to arguments[0] }
        // Triple Integer Argument
        types.size == 3 && types.all { it == PsiType.INT } ->
            handleThreeArguments(arguments)?.let { it to methodExpression }
        // Single Vector3* Argument
        types.size == 1 && arguments[0] is UCallExpression -> {
            val scope = GlobalSearchScope.allScope(project)
            when (types[0]) {
                PsiType.getTypeByName("com.flowpowered.math.vector.Vector3i", project, scope),
                PsiType.getTypeByName("com.flowpowered.math.vector.Vector3f", project, scope),
                PsiType.getTypeByName("com.flowpowered.math.vector.Vector3d", project, scope) ->
                    (arguments[0] as UCallExpression).takeIf { it.valueArgumentCount == 3 }
                        ?.let { handleThreeArguments(it.valueArguments) }
                        ?.let { it to arguments[0] }
                else -> null
            }
        }
        else -> null
    }
}

private fun handleSingleArgument(expression: UExpression): Color? {
    return Color(expression.evaluate() as? Int ?: return null)
}

private fun handleThreeArguments(expressions: List<UExpression>): Color? {
    fun normalize(value: Any?): Int? = when (value) {
        is Int -> value
        is Float -> round(value).toInt()
        is Double -> round(value).toInt()
        else -> null
    }

    val r = normalize(expressions[0].evaluate()) ?: return null
    val g = normalize(expressions[1].evaluate()) ?: return null
    val b = normalize(expressions[2].evaluate()) ?: return null
    return Color(r, g, b)
}
