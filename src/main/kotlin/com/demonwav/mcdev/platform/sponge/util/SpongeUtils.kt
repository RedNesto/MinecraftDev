package com.demonwav.mcdev.platform.sponge.util

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMember

fun PsiMember.isInSpongePluginClass(): Boolean = this.containingClass?.isSpongePluginClass() == true

fun PsiClass.isSpongePluginClass(): Boolean = this.hasAnnotation(SpongeConstants.PLUGIN_ANNOTATION)
