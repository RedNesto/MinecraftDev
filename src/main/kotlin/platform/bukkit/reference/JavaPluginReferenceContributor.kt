/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.bukkit.reference

import com.demonwav.mcdev.facet.MinecraftFacet
import com.demonwav.mcdev.platform.bukkit.BukkitModuleType
import com.demonwav.mcdev.platform.bukkit.PaperModuleType
import com.demonwav.mcdev.platform.bukkit.SpigotModuleType
import com.demonwav.mcdev.platform.bukkit.util.BukkitConstants
import com.demonwav.mcdev.util.constantStringValue
import com.demonwav.mcdev.util.findModule
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.patterns.PsiJavaPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.patterns.uast.callExpression
import com.intellij.patterns.uast.injectionHostUExpression
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.UastInjectionHostReferenceProvider
import com.intellij.psi.registerUastReferenceProvider
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ArrayUtil
import com.intellij.util.ProcessingContext
import java.util.Locale
import org.jetbrains.uast.UExpression
import org.jetbrains.yaml.YAMLUtil
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping

class JavaPluginReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        val getCommandMethod = PsiJavaPatterns.psiMethod()
            .withName("getCommand")
            .definedInClass(BukkitConstants.JAVA_PLUGIN)
        val getCommandNameLiteral = injectionHostUExpression()
            .callParameter(0, callExpression().withAnyResolvedMethod(getCommandMethod))

        val serverGetPluginCommandMethod = PsiJavaPatterns.psiMethod()
            .withName("getPluginCommand")
            .definedInClass(BukkitConstants.SERVER)
        val getPluginCommandNameLiteral = injectionHostUExpression()
            .callParameter(0, callExpression().withAnyResolvedMethod(serverGetPluginCommandMethod))

        val allPluginCommandLiterals = StandardPatterns.or(getCommandNameLiteral, getPluginCommandNameLiteral)
        registrar.registerUastReferenceProvider(allPluginCommandLiterals, PluginCommands)
    }
}

private object PluginCommands : UastInjectionHostReferenceProvider() {

    override fun getReferencesForInjectionHost(
        uExpression: UExpression,
        host: PsiLanguageInjectionHost,
        context: ProcessingContext
    ): Array<PsiReference> = arrayOf(BukkitPluginCommandReference(uExpression))
}

class BukkitPluginCommandReference(element: UExpression) : PsiReferenceBase<PsiElement>(element.sourcePsi!!, false) {
    override fun resolve(): PsiElement? {
        val originalLiteral = element.constantStringValue ?: return null
        val commandAlias = originalLiteral.toLowerCase(Locale.ENGLISH).substringAfter(':')
        val manifestFile = findManifestFile(this.element) ?: return null
        return YAMLUtil.getQualifiedKeyInFile(manifestFile, "commands", commandAlias)
    }

    override fun getVariants(): Array<Any> {
        val module = element.findModule() ?: return ArrayUtil.EMPTY_OBJECT_ARRAY
        val scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
        return collectAllCommandsVariants(element.project, scope, true)
    }
}

private fun findManifestFile(context: PsiElement): YAMLFile? {
    val module = context.findModule() ?: return null
    val manifestVFile = MinecraftFacet.getInstance(module, BukkitModuleType, SpigotModuleType, PaperModuleType)
        ?.pluginYml ?: return null
    val manifestDocument = FileDocumentManager.getInstance().getDocument(manifestVFile)
        ?: return null
    return PsiDocumentManager.getInstance(context.project).getPsiFile(manifestDocument) as? YAMLFile
}

private fun collectCommands(file: YAMLFile): List<String> {
    val commandsNode = YAMLUtil.getQualifiedKeyInFile(file, "commands") ?: return emptyList()
    val commandsMapping = commandsNode.value as? YAMLMapping ?: return emptyList()
    return commandsMapping.keyValues.map { it.keyText.toLowerCase(Locale.ENGLISH) }
}

private fun collectCommandsVariants(file: YAMLFile, includeNamespaces: Boolean = false): Array<Any> {
    val declaredCommands = collectCommands(file)
    if (declaredCommands.isEmpty()) {
        return ArrayUtil.EMPTY_OBJECT_ARRAY
    }

    val variants = declaredCommands.toMutableList<Any>()
    if (includeNamespaces) {
        val namespace = YAMLUtil.getValue(file, "name")?.second?.toLowerCase(Locale.ENGLISH)
        if (namespace != null) {
            declaredCommands.mapTo(variants) { alias ->
                // Give qualified commands a low priority because they are rarely used (from my experience at least)
                PrioritizedLookupElement.withPriority(LookupElementBuilder.create("$namespace:$alias"), -1.0)
            }
        }
    }
    return variants.toTypedArray()
}

private fun collectAllCommandsVariants(
    project: Project,
    scope: GlobalSearchScope,
    includeNamespaces: Boolean = false
): Array<Any> {
    val variants = mutableListOf<Any>()
    FilenameIndex.getFilesByName(project, "plugin.yml", scope).forEach { file ->
        if (file is YAMLFile) variants.addAll(collectCommandsVariants(file, includeNamespaces))
    }
    return variants.toTypedArray()
}
