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

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import org.jetbrains.intellij.platform.gradle.extensions.IntelliJPlatformDependenciesExtension

fun Project.registerLexer(flex: String, pack: String, name: String = "generate$flex"): TaskProvider<JFlexExec> {
    extensions.configure<PatternFilterable>("license") {
        exclude(pack.removeSuffix("/") + "/**")
    }

    val provider = tasks.register<JFlexExec>(name) {
        sourceFile.set(layout.projectDirectory.file("src/main/grammars/$flex.flex"))
        destinationDirectory.set(layout.buildDirectory.dir("gen/$pack/lexer"))
        destinationFile.set(layout.buildDirectory.file("gen/$pack/lexer/$flex.java"))
        logFile.set(layout.buildDirectory.file("logs/generate$flex.log"))

        val jflex by project.configurations
        this.jflex.setFrom(jflex)

        val jflexSkeleton by project.configurations
        skeletonFile.set(jflexSkeleton.singleFile)
    }

    tasks.named("generate") {
        dependsOn(provider)
    }

    return provider
}

fun Project.registerParser(bnf: String, pack: String, name: String = "generate$bnf"): TaskProvider<ParserExec> {
    extensions.configure<PatternFilterable>("license") {
        exclude(pack.removeSuffix("/") + "/**")
    }

    val provider = tasks.register<ParserExec>(name) {
        val destRoot = project.layout.buildDirectory.dir("gen")
        val dest = destRoot.map { it.dir(pack) }
        sourceFile.set(project.layout.projectDirectory.file("src/main/grammars/$bnf.bnf"))
        destinationRootDirectory.set(destRoot)
        psiDirectory.set(dest.map { it.dir("psi") })
        parserDirectory.set(dest.map { it.dir("parser") })
        logFile.set(layout.buildDirectory.file("logs/generate$bnf.log"))

        val grammarKit by project.configurations
        this.grammarKit.setFrom(grammarKit)
    }

    tasks.named("generate") {
        dependsOn(provider)
    }

    return provider
}

fun IntelliJPlatformDependenciesExtension.registerMcDevDependencies() {
    bundledPlugin("com.intellij.java")
    bundledPlugin("org.jetbrains.idea.maven")
    bundledPlugin("com.intellij.gradle")
    bundledPlugin("org.intellij.groovy")
    bundledPlugin("ByteCodeViewer")
    bundledPlugin("org.intellij.intelliLang")
    bundledPlugin("com.intellij.properties")
}
