/*
 * Minecraft Development for IntelliJ
 *
 * https://mcdev.io/
 *
 * Copyright (C) 2024 minecraft-dev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, version 3.0 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.demonwav.mcdev.kotlin.creator.buildsystem

import com.demonwav.mcdev.creator.buildsystem.BuildDependency
import com.demonwav.mcdev.creator.buildsystem.BuildRepository
import com.demonwav.mcdev.creator.buildsystem.BuildSystemType
import com.demonwav.mcdev.creator.buildsystem.GradleFile
import com.demonwav.mcdev.creator.buildsystem.GradlePlugin
import com.demonwav.mcdev.creator.buildsystem.escapeGString
import com.demonwav.mcdev.creator.buildsystem.makePluginStatement
import com.demonwav.mcdev.creator.buildsystem.makeStringLiteral
import com.demonwav.mcdev.util.childrenOfType
import com.demonwav.mcdev.util.mapFirstNotNull
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtScriptInitializer

class KotlinGradleFile(override val psi: KtFile) : GradleFile {
    override fun addRepositories(project: Project, repositories: List<BuildRepository>) {
        val script = psi.script?.blockExpression ?: return
        val reposBlock = findOrCreateKotlinBlock(project, script, "repositories")
        val elementFactory = KtPsiFactory(project)
        for (repo in repositories) {
            if (BuildSystemType.GRADLE !in repo.buildSystems) {
                continue
            }
            val mavenBlock = elementFactory.createExpression("maven {\n}") as KtCallExpression
            val mavenLambda = mavenBlock.lambdaArguments[0].getLambdaExpression()!!.bodyExpression!!
            if (repo.id.isNotBlank()) {
                val idStatement = elementFactory.createAssignment("name = ${makeStringLiteral(repo.id)}")
                mavenLambda.addBefore(idStatement, mavenLambda.rBrace)
            }
            val urlStatement = elementFactory.createAssignment("url = uri(${makeStringLiteral(repo.url)})")
            mavenLambda.addBefore(urlStatement, mavenLambda.rBrace)
            reposBlock.addBefore(mavenBlock, reposBlock.rBrace)
        }
    }

    override fun addDependencies(project: Project, dependencies: List<BuildDependency>) {
        val script = psi.script?.blockExpression ?: return
        val depsBlock = findOrCreateKotlinBlock(project, script, "dependencies")
        val elementFactory = org.jetbrains.kotlin.psi.KtPsiFactory(project)
        for (dep in dependencies) {
            val gradleConfig = dep.gradleConfiguration ?: continue
            val stmt = elementFactory.createExpression(
                "$gradleConfig(\"${escapeGString(dep.groupId)}:${
                    escapeGString(dep.artifactId)
                }:${escapeGString(dep.version)}\")",
            )
            depsBlock.addBefore(stmt, depsBlock.rBrace)
        }
    }

    override fun addPlugins(project: Project, plugins: List<GradlePlugin>) {
        val script = psi.script?.blockExpression ?: return
        val pluginsBlock = findOrCreateKotlinBlock(project, script, "plugins", first = true)
        val elementFactory = org.jetbrains.kotlin.psi.KtPsiFactory(project)
        for (plugin in plugins) {
            val stmt = elementFactory.createExpression(makePluginStatement(plugin, true))
            pluginsBlock.addBefore(stmt, pluginsBlock.rBrace)
        }
    }

    private fun findKotlinBlock(element: KtBlockExpression, name: String): KtBlockExpression? {
        return element.childrenOfType<KtScriptInitializer>()
            .flatMap { it.childrenOfType<KtCallExpression>() }
            .mapFirstNotNull { call ->
                if ((call.calleeExpression as? KtNameReferenceExpression)?.getReferencedName() == name) {
                    call.lambdaArguments.firstOrNull()?.getLambdaExpression()?.bodyExpression
                } else {
                    null
                }
            }
    }

    private fun findOrCreateKotlinBlock(
        project: Project,
        element: KtBlockExpression,
        name: String,
        first: Boolean = false,
    ): KtBlockExpression {
        findKotlinBlock(element, name)?.let { return it }
        val block = org.jetbrains.kotlin.psi.KtPsiFactory(project).createExpression("$name {\n}")
        val addedBlock = if (first) {
            element.addAfter(block, element.lBrace)
        } else {
            element.addBefore(block, element.rBrace)
        }
        return (addedBlock as KtCallExpression).lambdaArguments.first().getLambdaExpression()!!.bodyExpression!!
    }

    private fun KtPsiFactory.createAssignment(text: String): KtBinaryExpression {
        return this.createBlock(text).firstStatement as KtBinaryExpression
    }

    class Type : GradleFile.Type {
        override fun createGradleFile(psiFile: PsiFile) = (psiFile as? KtFile)?.let(::KotlinGradleFile)
    }
}
