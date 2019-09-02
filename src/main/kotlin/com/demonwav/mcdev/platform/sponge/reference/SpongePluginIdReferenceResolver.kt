package com.demonwav.mcdev.platform.sponge.reference

import com.demonwav.mcdev.platform.sponge.util.SpongeConstants
import com.demonwav.mcdev.util.constantStringValue
import com.demonwav.mcdev.util.findContainingClass
import com.demonwav.mcdev.util.findModule
import com.demonwav.mcdev.util.mapFirstNotNull
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiAnnotationMemberValue
import com.intellij.psi.PsiArrayInitializerMemberValue
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.parentOfType
import com.intellij.util.ArrayUtil
import com.intellij.util.ProcessingContext
import com.intellij.util.Query
import org.jetbrains.plugins.groovy.lang.psi.impl.stringValue

object SpongePluginIdReferenceResolver : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val pluginId = element.constantStringValue ?: return PsiReference.EMPTY_ARRAY
        return arrayOf(Ref(element, pluginId))
    }

    class Ref(element: PsiElement, private val pluginId: String) : PsiReferenceBase<PsiElement>(element) {
        override fun resolve(): PsiElement? =
            findAttributeValueDeclaringPluginId(pluginId, element)?.findContainingClass()

        override fun getVariants(): Array<Any> {
            var thisPluginId: String? = null
            val dependencies: MutableList<String> = mutableListOf()

            val parentAnnotation = element.parentOfType<PsiAnnotation>()
            if (parentAnnotation != null) {
                if (parentAnnotation.hasQualifiedName(SpongeConstants.PLUGIN_ANNOTATION)) {
                    // We do not want to suggest plugin ids to a plugin id declaration, inside @Plugin(id = "")
                    return ArrayUtil.EMPTY_OBJECT_ARRAY
                }

                if (parentAnnotation.hasQualifiedName(SpongeConstants.DEPENDENCY_ANNOTATION)) {
                    val grandparentAnnotation = parentAnnotation.parentOfType<PsiAnnotation>()
                    thisPluginId = grandparentAnnotation?.findAttributeValue("id")?.stringValue()
                    val memberValue = parentAnnotation.parent
                    if (memberValue is PsiArrayInitializerMemberValue) {
                        memberValue.initializers.forEach {
                            val dependencyId = (it as? PsiAnnotation)?.findAttributeValue("id")?.stringValue()
                            if (dependencyId != null && isValidPluginId(dependencyId)) {
                                dependencies += dependencyId
                            }
                        }
                    } else {
                        parentAnnotation.findAttributeValue("id")?.stringValue()?.let(dependencies::add)
                    }
                }
            }

            val pluginIdDeclarations = collectSpongePluginIdDeclarations(element)
            return pluginIdDeclarations.mapNotNull { idDeclaration ->
                val pluginId = idDeclaration.stringValue() ?: return@mapNotNull null
                if (pluginId == thisPluginId || !isValidPluginId(pluginId) || pluginId in dependencies) {
                    return@mapNotNull null
                }

                val pluginAnnotation = idDeclaration.parentOfType<PsiAnnotation>()
                val pluginName = pluginAnnotation?.findAttributeValue("name")?.stringValue()
                var elementBuilder = LookupElementBuilder.create(pluginId)
                if (pluginName != null) {
                    elementBuilder = elementBuilder.withTypeText(pluginName)
                }
                elementBuilder
            }.toTypedArray()
        }
    }
}

private fun collectSpongePluginIdDeclarations(context: PsiElement): List<PsiAnnotationMemberValue> {
    val references = getPluginAnnotationReferences(context) ?: return emptyList()
    return references.mapNotNull { it.element.parentOfType<PsiAnnotation>()?.findAttributeValue("id") }
}

private fun findAttributeValueDeclaringPluginId(pluginId: String, context: PsiElement): PsiAnnotationMemberValue? {
    val references = getPluginAnnotationReferences(context) ?: return null
    return references.mapFirstNotNull { ref ->
        val pluginAnno = ref.element.parentOfType<PsiAnnotation>() ?: return@mapFirstNotNull null
        val idAttribute = pluginAnno.findAttributeValue("id") ?: return@mapFirstNotNull null
        if (idAttribute.constantStringValue == pluginId) {
            return@mapFirstNotNull idAttribute
        }

        return@mapFirstNotNull null
    }
}

private fun getPluginAnnotationReferences(context: PsiElement): Query<PsiReference>? {
    val findModule = context.findModule() ?: return null
    val psiFacade = JavaPsiFacade.getInstance(context.project)
    val pluginAnnoClass = psiFacade.findClass(
        SpongeConstants.PLUGIN_ANNOTATION,
        GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(findModule)
    ) ?: return null
    return ReferencesSearch.search(pluginAnnoClass)
}

private fun isValidPluginId(pluginId: String): Boolean = pluginId.matches(ID_REGEX)

// Replace by constants from SpongeConstants once https://github.com/minecraft-dev/MinecraftDev/pull/641 is merged
private val ID_REGEX = "^[a-z][a-z0-9-_]{1,63}$".toRegex()
