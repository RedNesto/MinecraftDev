package com.demonwav.mcdev.platform.sponge.util

import com.demonwav.mcdev.util.constantStringValue
import com.demonwav.mcdev.util.findContainingClass
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMember

fun PsiMember.isInSpongePluginClass(): Boolean = this.containingClass?.isSpongePluginClass() == true

fun PsiClass.isSpongePluginClass(): Boolean = this.hasAnnotation(SpongeConstants.PLUGIN_ANNOTATION)

fun PsiElement.spongePluginClassId(): String? = this.findContainingClass()
    ?.getAnnotation(SpongeConstants.PLUGIN_ANNOTATION)?.findAttributeValue("id")?.constantStringValue
