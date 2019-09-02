/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2019 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.sponge.reference

import com.demonwav.mcdev.platform.sponge.util.SpongeConstants
import com.demonwav.mcdev.util.insideAnnotationAttribute
import com.intellij.patterns.PsiJavaPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar

class SpongeReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(PsiJavaPatterns.psiLiteral(StandardPatterns.string())
                .insideAnnotationAttribute(SpongeConstants.GETTER_ANNOTATION), GetterEventListenerReferenceResolver)

        registrar.registerReferenceProvider(PsiJavaPatterns.or(
            // @Plugin(String id)
            PsiJavaPatterns.psiLiteral(StandardPatterns.string())
                .insideAnnotationAttribute(SpongeConstants.PLUGIN_ANNOTATION, "id"),
            // @Dependency(String id)
            PsiJavaPatterns.psiLiteral(StandardPatterns.string())
                .insideAnnotationAttribute(SpongeConstants.DEPENDENCY_ANNOTATION, "id"),
            // PluginManager#getPlugin(String pluginId)
            PsiJavaPatterns.psiLiteral(StandardPatterns.string())
                .methodCallParameter(PsiJavaPatterns.psiMethod()
                    .definedInClass(SpongeConstants.PLUGIN_MANAGER)
                    .withName("getPlugin")),
            // PluginManager#isLoaded(String pluginId)
            PsiJavaPatterns.psiLiteral(StandardPatterns.string())
                .methodCallParameter(PsiJavaPatterns.psiMethod()
                    .definedInClass(SpongeConstants.PLUGIN_MANAGER)
                    .withName("isLoaded")),
            // GameRegistry#getAllFor(String pluginId, Class<CatalogType> typeClass)
            PsiJavaPatterns.psiLiteral(StandardPatterns.string())
                .methodCallParameter(PsiJavaPatterns.psiMethod()
                    .definedInClass(SpongeConstants.GAME_REGISTRY)
                    .withName("getAllFor"))
        ), SpongePluginIdReferenceResolver)
    }
}
