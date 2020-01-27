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

import com.demonwav.mcdev.facet.MinecraftFacet
import com.demonwav.mcdev.platform.bukkit.BukkitModuleType
import com.demonwav.mcdev.platform.bukkit.PaperModuleType
import com.demonwav.mcdev.platform.bukkit.SpigotModuleType
import com.demonwav.mcdev.platform.bukkit.util.BukkitConstants
import com.demonwav.mcdev.util.constantStringValue
import com.demonwav.mcdev.util.findModule
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.patterns.PsiJavaPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ArrayUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.YAMLUtil
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping

class JavaPluginReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        val getCommandMethod = PsiJavaPatterns.psiMethod()
            .withName("getCommand")
            .definedInClass(BukkitConstants.JAVA_PLUGIN)
        val getCommandNameLiteral = PsiJavaPatterns.psiLiteral(StandardPatterns.string())
            .methodCallParameter(0, getCommandMethod)
        registrar.registerReferenceProvider(getCommandNameLiteral, PluginCommands)
    }
}

private object PluginCommands : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> =
        arrayOf(BukkitPluginCommandReference(element))
}

class BukkitPluginCommandReference(element: PsiElement) : PsiReferenceBase<PsiElement>(element, false) {
    override fun resolve(): PsiElement? {
        val command = element.constantStringValue ?: return null
        val manifestFile = findManifestFile(this.element) ?: return null
        return YAMLUtil.getQualifiedKeyInFile(manifestFile, "commands", command)
    }

    override fun getVariants(): Array<Any> {
        val manifest = findManifestFile(element) ?: return ArrayUtil.EMPTY_OBJECT_ARRAY
        return collectCommands(manifest).toTypedArray()
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
    return commandsMapping.keyValues.map { it.keyText }
}
