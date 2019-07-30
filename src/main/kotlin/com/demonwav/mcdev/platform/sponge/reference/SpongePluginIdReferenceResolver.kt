package com.demonwav.mcdev.platform.sponge.reference

import com.demonwav.mcdev.platform.sponge.util.SpongeConstants
import com.demonwav.mcdev.util.constantStringValue
import com.demonwav.mcdev.util.findModule
import com.demonwav.mcdev.util.mapFirstNotNull
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiAnnotationMemberValue
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext

object SpongePluginIdReferenceResolver : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val pluginId = element.constantStringValue ?: return PsiReference.EMPTY_ARRAY
        return arrayOf(Ref(element, pluginId))
    }

    class Ref(element: PsiElement, private val pluginId: String) : PsiReferenceBase<PsiElement>(element) {
        override fun resolve(): PsiElement? = findAttributeValueDeclaringPluginId(pluginId, element)

        override fun getVariants(): Array<Any> = collectSpongePluginIds(element).toTypedArray()
    }
}

private fun collectSpongePluginIds(context: PsiElement): List<String> =
    collectSpongePluginIdsDeclarations(context).mapNotNull { it.constantStringValue }

private fun collectSpongePluginIdsDeclarations(context: PsiElement): List<PsiAnnotationMemberValue> {
    val findModule = context.findModule() ?: return emptyList()
    val psiFacade = JavaPsiFacade.getInstance(context.project)
    val pluginAnnoClass = psiFacade.findClass(
        SpongeConstants.PLUGIN_ANNOTATION,
        GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(findModule)
    ) ?: return emptyList()
    val references = ReferencesSearch.search(pluginAnnoClass)
    return references.mapNotNull { it.element.parentOfType<PsiAnnotation>()?.findAttributeValue("id") }
}

private fun findAttributeValueDeclaringPluginId(pluginId: String, context: PsiElement): PsiAnnotationMemberValue? {
    val findModule = context.findModule() ?: return null
    val psiFacade = JavaPsiFacade.getInstance(context.project)
    val pluginAnnoClass = psiFacade.findClass(
        SpongeConstants.PLUGIN_ANNOTATION,
        GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(findModule)
    ) ?: return null
    val references = ReferencesSearch.search(pluginAnnoClass)
    return references.mapFirstNotNull { ref ->
        val pluginAnno = ref.element.parentOfType<PsiAnnotation>() ?: return@mapFirstNotNull null
        val idAttribute = pluginAnno.findAttributeValue("id") ?: return@mapFirstNotNull null
        return@mapFirstNotNull if (idAttribute.constantStringValue == pluginId) {
            idAttribute
        } else {
            null
        }
    }
}
