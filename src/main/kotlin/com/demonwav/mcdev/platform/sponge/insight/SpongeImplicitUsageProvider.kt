package com.demonwav.mcdev.platform.sponge.insight

import com.demonwav.mcdev.platform.sponge.util.SpongeConstants
import com.demonwav.mcdev.platform.sponge.util.isInSpongePluginClass
import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod

class SpongeImplicitUsageProvider : ImplicitUsageProvider {
    override fun isImplicitWrite(element: PsiElement): Boolean = isPluginClassInjectedField(element, false)

    override fun isImplicitRead(element: PsiElement): Boolean = false

    override fun isImplicitUsage(element: PsiElement): Boolean = isPluginClassEmptyConstructor(element)

    override fun isImplicitlyNotNullInitialized(element: PsiElement): Boolean = isPluginClassInjectedField(element, true)

    private fun isPluginClassEmptyConstructor(element: PsiElement): Boolean {
        return element is PsiMethod && element.isInSpongePluginClass() && element.isConstructor && !element.hasParameters()
    }

    private fun isPluginClassInjectedField(element: PsiElement, optionalSensitive: Boolean): Boolean {
        if (element is PsiField && element.isInSpongePluginClass() && element.hasAnnotation(SpongeConstants.INJECT_ANNOTATION)) {
            val annotation = element.getAnnotation(SpongeConstants.INJECT_ANNOTATION) ?: return false
            if (!optionalSensitive) {
                return true
            }

            val optional = annotation.findAttribute("optional") ?: return true
            val value = optional.attributeValue
            return value is JvmAnnotationConstantValue && value.constantValue == false
        }
        return false
    }
}
