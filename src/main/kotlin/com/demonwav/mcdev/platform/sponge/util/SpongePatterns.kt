/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2019 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.sponge.util

import com.demonwav.mcdev.util.insideAnnotationAttribute
import com.intellij.patterns.PsiJavaPatterns
import com.intellij.patterns.StandardPatterns

object SpongePatterns {

    val GETTER_ANNOTATION_VALUE = PsiJavaPatterns.psiLiteral(StandardPatterns.string())
        .insideAnnotationAttribute(SpongeConstants.GETTER_ANNOTATION)!!

    /** `@Plugin(String id)` */
    val PLUGIN_ANNOTATION_ID = PsiJavaPatterns.psiLiteral(StandardPatterns.string())
        .insideAnnotationAttribute(SpongeConstants.PLUGIN_ANNOTATION, "id")!!
    val PLUGIN_ANNOTATION_AUTHORS = PsiJavaPatterns.psiLiteral(StandardPatterns.string())
        .insideAnnotationAttribute(SpongeConstants.PLUGIN_ANNOTATION, "authors")!!
    /** `@Dependency(String id)` */
    val DEPENDENCY_ANNOTATION_ID = PsiJavaPatterns.psiLiteral(StandardPatterns.string())
        .insideAnnotationAttribute(SpongeConstants.DEPENDENCY_ANNOTATION, "id")!!
    /** PluginManager#getPlugin(String pluginId) */
    val PLUGIN_MANAGER_GET_PLUGIN_ARG = PsiJavaPatterns.psiLiteral(StandardPatterns.string())
        .methodCallParameter(
            PsiJavaPatterns.psiMethod()
                .definedInClass(SpongeConstants.PLUGIN_MANAGER)
                .withName("getPlugin")
        )!!
    /** PluginManager#isLoaded(String pluginId) */
    val PLUGIN_MANAGER_IS_LOADED_ARG = PsiJavaPatterns.psiLiteral(StandardPatterns.string())
        .methodCallParameter(
            PsiJavaPatterns.psiMethod()
                .definedInClass(SpongeConstants.PLUGIN_MANAGER)
                .withName("isLoaded")
        )!!
    /** GameRegistry#getAllFor(String pluginId, Class<CatalogType> typeClass) */
    val GAME_REGISTRY_GET_ALL_FOR_ARG0 = PsiJavaPatterns.psiLiteral(StandardPatterns.string())
        .methodCallParameter(
            0,
            PsiJavaPatterns.psiMethod()
                .definedInClass(SpongeConstants.GAME_REGISTRY)
                .withName("getAllFor")
        )!!

    val PLUGIN_ID_USAGES = PsiJavaPatterns.or(
        DEPENDENCY_ANNOTATION_ID,
        PLUGIN_MANAGER_GET_PLUGIN_ARG,
        PLUGIN_MANAGER_IS_LOADED_ARG,
        GAME_REGISTRY_GET_ALL_FOR_ARG0
    )

    val PLUGIN_ID_LITERALS = PsiJavaPatterns.or(PLUGIN_ANNOTATION_ID, PLUGIN_ID_USAGES)
}
