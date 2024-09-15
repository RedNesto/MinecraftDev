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

package com.demonwav.mcdev.creator.buildsystem

import com.demonwav.mcdev.util.mapFirstNotNull
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.ApiStatus.Internal
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression
import org.jetbrains.plugins.groovy.lang.psi.api.util.GrStatementOwner

@Internal
interface GradleFile {
    companion object {
        val EP_NAME = ExtensionPointName<Type>("com.demonwav.minecraft-dev.gradleFileType")
    }

    val psi: PsiFile

    fun addRepositories(project: Project, repositories: List<BuildRepository>)
    fun addDependencies(project: Project, dependencies: List<BuildDependency>)
    fun addPlugins(project: Project, plugins: List<GradlePlugin>)

    interface Type {
        fun createGradleFile(psiFile: PsiFile): GradleFile?
    }
}

class GroovyGradleFile(override val psi: GroovyFile) : GradleFile {
    override fun addRepositories(project: Project, repositories: List<BuildRepository>) {
        val reposBlock = findOrCreateGroovyBlock(project, psi, "repositories")
        val elementFactory = GroovyPsiElementFactory.getInstance(project)
        for (repo in repositories) {
            if (BuildSystemType.GRADLE !in repo.buildSystems) {
                continue
            }
            val mavenBlock =
                elementFactory.createStatementFromText("maven {\n}", reposBlock) as GrMethodCallExpression
            val mavenClosure = mavenBlock.closureArguments[0]
            if (repo.id.isNotBlank()) {
                val idStatement =
                    elementFactory.createStatementFromText("name = ${makeStringLiteral(repo.id)}")
                mavenClosure.addStatementBefore(idStatement, null)
            }
            val urlStatement =
                elementFactory.createStatementFromText("url = ${makeStringLiteral(repo.url)}")
            mavenClosure.addStatementBefore(urlStatement, null)
            reposBlock.addStatementBefore(mavenBlock, null)
        }
    }

    override fun addDependencies(project: Project, dependencies: List<BuildDependency>) {
        val depsBlock = findOrCreateGroovyBlock(project, psi, "dependencies")
        val elementFactory = GroovyPsiElementFactory.getInstance(project)
        for (dep in dependencies) {
            val gradleConfig = dep.gradleConfiguration ?: continue
            val stmt = elementFactory.createStatementFromText(
                "$gradleConfig \"${escapeGString(dep.groupId)}:${
                escapeGString(dep.artifactId)
                }:${escapeGString(dep.version)}\"",
                depsBlock,
            )
            depsBlock.addStatementBefore(stmt, null)
        }
    }

    override fun addPlugins(project: Project, plugins: List<GradlePlugin>) {
        val pluginsBlock = findOrCreateGroovyBlock(project, psi, "plugins", first = true)
        val elementFactory = GroovyPsiElementFactory.getInstance(project)
        for (plugin in plugins) {
            val stmt = elementFactory.createStatementFromText(makePluginStatement(plugin, false))
            pluginsBlock.addStatementBefore(stmt, null)
        }
    }

    private fun findGroovyBlock(element: GrStatementOwner, name: String): GrClosableBlock? {
        return element.statements
            .mapFirstNotNull { call ->
                if (call is GrMethodCallExpression && call.callReference?.methodName == name) {
                    call.closureArguments.firstOrNull()
                } else {
                    null
                }
            }
    }

    private fun findOrCreateGroovyBlock(
        project: Project,
        element: GrStatementOwner,
        name: String,
        first: Boolean = false,
    ): GrClosableBlock {
        findGroovyBlock(element, name)?.let { return it }
        val block = GroovyPsiElementFactory.getInstance(project).createStatementFromText("$name {\n}", element)
        val anchor = if (first) {
            element.statements.firstOrNull()
        } else {
            null
        }
        return (element.addStatementBefore(block, anchor) as GrMethodCallExpression).closureArguments.first()
    }

    class Type : GradleFile.Type {
        override fun createGradleFile(psiFile: PsiFile) = (psiFile as? GroovyFile)?.let(::GroovyGradleFile)
    }
}

fun makeStringLiteral(str: String): String {
    return "\"${escapeGString(str)}\""
}

fun escapeGString(str: String): String {
    return StringUtil.escapeStringCharacters(str.length, str, "\"\$", StringBuilder()).toString()
}

fun makePluginStatement(plugin: GradlePlugin, kotlin: Boolean): String {
    return buildString {
        if (kotlin) {
            append("id(${makeStringLiteral(plugin.id)})")
        } else {
            append("id ${makeStringLiteral(plugin.id)}")
        }
        plugin.version?.let { append(" version ${makeStringLiteral(it)}") }
        if (!plugin.apply) {
            append(" apply false")
        }
    }
}
