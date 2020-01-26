/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2019 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.bukkit.reference

import com.demonwav.mcdev.platform.bukkit.util.BukkitConstants
import com.demonwav.mcdev.util.findModule
import com.demonwav.mcdev.util.reference.ClassNameReferenceProvider
import com.demonwav.mcdev.util.yamlFileNamed
import com.demonwav.mcdev.util.yamlValueByKey
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.PsiUtil

class PluginManifestReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        val manifestFile = yamlFileNamed("plugin.yml")

        registrar.registerReferenceProvider(yamlValueByKey("main").inside(manifestFile), MainPluginClass)
    }
}

private object MainPluginClass : ClassNameReferenceProvider() {
    override fun findClasses(element: PsiElement, scope: GlobalSearchScope): List<PsiClass> {
        val module = element.findModule() ?: return emptyList()
        val searchScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
        val bukkitPluginClass = JavaPsiFacade.getInstance(element.project)
            .findClass(BukkitConstants.PLUGIN, searchScope) ?: return emptyList()
        return ClassInheritorsSearch.search(bukkitPluginClass, searchScope, true, true, false)
            .filterNot(PsiUtil::isAbstractClass)
            .toList()
    }
}
