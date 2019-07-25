package com.demonwav.mcdev.platform.sponge.reference

import com.demonwav.mcdev.platform.sponge.util.SpongeConstants
import com.demonwav.mcdev.platform.sponge.util.spongePluginClassId
import com.demonwav.mcdev.util.constantStringValue
import com.demonwav.mcdev.util.findModule
import com.demonwav.mcdev.util.insideAnnotationAttribute
import com.demonwav.mcdev.util.reference.PolyReferenceResolver
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.patterns.PsiJavaPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.JavaTokenType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext

class SpongeAssetReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PsiJavaPatterns.psiLiteral(StandardPatterns.string())
                .insideAnnotationAttribute(SpongeConstants.ASSET_ID_ANNOTATION, "value"),
            AssetPathReferenceResolver()
        )
    }
}

class AssetPathReferenceResolver : PolyReferenceResolver() {
    override fun resolveReference(context: PsiElement): Array<ResolveResult> {
        val module = context.findModule() ?: return ResolveResult.EMPTY_ARRAY
        val pluginId = context.spongePluginClassId() ?: return ResolveResult.EMPTY_ARRAY
        val roots = ModuleRootManager.getInstance(module).getSourceRoots(false)
        val relativeAssetPath = "assets/$pluginId/${context.constantStringValue}"
        return roots.mapNotNull {
            val foundFile = it.findFileByRelativePath(relativeAssetPath) ?: return@mapNotNull null
            val psiFile = if (foundFile.isDirectory) {
                context.manager.findDirectory(foundFile)
            } else {
                context.manager.findFile(foundFile)
            } ?: return@mapNotNull null
            PsiElementResolveResult(psiFile)
        }.toTypedArray()
    }

    override fun collectVariants(context: PsiElement): Array<Any> {
        return emptyArray()
        //val module = context.findModule() ?: return ArrayUtil.EMPTY_OBJECT_ARRAY
        //val pluginId = context.spongePluginClassId() ?: return ArrayUtil.EMPTY_OBJECT_ARRAY
        //val roots = ModuleRootManager.getInstance(module).getSourceRoots(false)
        //val pluginAssetsDir = "assets/$pluginId"
        //val text = context.text
        //    .removePrefix("\"").removeSuffix("${CompletionUtilCore.DUMMY_IDENTIFIER}\"")
        //    .replace('\\', '/')
        //val pathToComplete = if (text.contains('/')) {
        //    val subDirs = text.substringBeforeLast('/')
        //    "$pluginAssetsDir/$subDirs"
        //} else pluginAssetsDir
        //return roots.flatMap { root ->
        //    val assetsDir = root.findFileByRelativePath(pathToComplete) ?: return@flatMap emptyList<Any>()
        //    assetsDir.children.map { child ->
        //        var name = child.name
        //        if (child.isDirectory) {
        //            name += '/'
        //        }
        //
        //        LookupElementBuilder.create(name, name).withTypeText(pluginId)
        //    }
        //}.toTypedArray()
    }
}

class SpongeAssetCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PsiJavaPatterns.psiElement(JavaTokenType.STRING_LITERAL)
                .insideAnnotationAttribute(SpongeConstants.ASSET_ID_ANNOTATION, "value"),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
                    val position = parameters.originalPosition?.parentOfType<PsiLiteralExpression>() ?: return
                    val module = position.findModule() ?: return
                    val pluginId = position.spongePluginClassId() ?: return
                    val roots = ModuleRootManager.getInstance(module).getSourceRoots(false)
                    val pluginAssetsDir = "assets/$pluginId"
                    val originalValue = position.constantStringValue ?: return
                    val text = originalValue.replace('\\', '/')
                    val relativeAssetsDir = if (roots.none { it.findFileByRelativePath("$pluginAssetsDir/$text")?.exists() == true }) {
                        val subDirs = text.substringBeforeLast('/')
                        subDirs
                    } else  {
                        text
                    }

                    val pathToComplete = if (text.contains('/')) "$pluginAssetsDir/$relativeAssetsDir" else pluginAssetsDir
                    roots.flatMap { root ->
                        val assetsDir = root.findFileByRelativePath(pathToComplete) ?: return@flatMap emptyList<LookupElement>()
                        assetsDir.children.map { child ->
                            val fileName = if (child.isDirectory) child.name + '/' else child.name
                            val completionName = if (relativeAssetsDir.isEmpty() || relativeAssetsDir.endsWith('/')) {
                                relativeAssetsDir + fileName
                            } else {
                                "$relativeAssetsDir/$fileName"
                            }

                            LookupElementBuilder.create(completionName).withPresentableText(fileName).withTypeText(pluginId)
                        }
                    }.forEach(result::addElement)
                    result.stopHere()
                }
            }
        )
    }
}
