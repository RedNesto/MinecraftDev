/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2019 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.mixin.reference

import com.demonwav.mcdev.platform.mixin.util.MixinConstants
import com.demonwav.mcdev.platform.mixin.util.findAccessorTarget
import com.demonwav.mcdev.platform.mixin.util.mixinTargets
import com.demonwav.mcdev.util.findContainingClass
import com.demonwav.mcdev.util.reference.PolyReferenceResolver
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiMember
import com.intellij.psi.PsiMethod
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.PropertyUtil
import com.intellij.psi.util.parentOfType
import com.intellij.util.ArrayUtil

object AccessorReference : PolyReferenceResolver(), MixinReference {

    override val description: String
        get() = "accessed field '%s' in target class"

    override fun isValidAnnotation(name: String) = name == MixinConstants.Annotations.ACCESSOR

    override fun isUnresolved(context: PsiElement): Boolean {
        return context.parentOfType<PsiMethod>()?.findAccessorTarget() == null
    }

    override fun resolveReference(context: PsiElement): Array<ResolveResult> {
        val target = context.parentOfType<PsiMethod>()?.findAccessorTarget()
            ?: return ResolveResult.EMPTY_ARRAY
        return arrayOf(PsiElementResolveResult(target))
    }

    override fun collectVariants(context: PsiElement): Array<Any> {
        val mixinTargets = context.findContainingClass()?.mixinTargets ?: return ArrayUtil.EMPTY_OBJECT_ARRAY
        val candidates = mutableListOf<PsiMember>()
        mixinTargets.forEach { targetClass ->
            candidates.addAll(targetClass.fields)
            targetClass.methods.forEach { method ->
                if (PropertyUtil.isSimpleGetter(method) || PropertyUtil.isSimpleSetter(method)) {
                    candidates.add(method)
                }
            }
        }
        return candidates.toTypedArray()
    }
}
