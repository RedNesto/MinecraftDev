/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.bukkit.inspection

import com.demonwav.mcdev.platform.bukkit.util.BukkitConstants
import com.demonwav.mcdev.util.findModule
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.annotations.Nls
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLValue
import org.jetbrains.yaml.psi.YamlPsiElementVisitor

class BukkitManifestValidationInspection : LocalInspectionTool() {

    @Nls
    override fun getDisplayName() =
        "Bukkit plugin manifest validation"

    override fun getStaticDescription() =
        "Validates Bukkit plugins manifests"

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (holder.file.name != "plugin.yml") {
            return PsiElementVisitor.EMPTY_VISITOR
        }
        return object : YamlPsiElementVisitor() {
            override fun visitKeyValue(keyValue: YAMLKeyValue) {
                val value = keyValue.value ?: return
                when (keyValue.keyText) {
                    "main" -> validateMainClass(holder, value)
                }
            }
        }
    }
}

private fun validateMainClass(holder: ProblemsHolder, value: YAMLValue) {
    val mainClass = JavaPsiFacade.getInstance(value.project)
        .findClass(value.text, value.resolveScope)
    if (mainClass == null) {
        holder.registerProblem(
            value,
            "Main class does not exist",
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING
        )
        return
    }

    value.findModule()?.let { module ->
        val searchScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
        val bukkitPlugin = JavaPsiFacade.getInstance(value.project)
            .findClass(BukkitConstants.PLUGIN, searchScope)
        if (bukkitPlugin != null && !mainClass.isInheritor(bukkitPlugin, true)) {
            holder.registerProblem(
                value,
                "Main class does not implement Plugin",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
            )
        }
    }
}
