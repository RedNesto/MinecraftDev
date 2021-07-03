/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.sponge.creator

import com.demonwav.mcdev.creator.buildsystem.BuildSystem
import com.demonwav.mcdev.creator.buildsystem.maven.BasicMavenStep
import com.demonwav.mcdev.platform.BaseTemplate
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.SPONGE_BUILD_GRADLE_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.SPONGE_GRADLE_PROPERTIES_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.SPONGE_MAIN_CLASS_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.SPONGE_POM_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.SPONGE_SETTINGS_GRADLE_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.SPONGE_SUBMODULE_BUILD_GRADLE_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.SPONGE_SUBMODULE_POM_TEMPLATE
import com.intellij.openapi.project.Project

object SpongeTemplate : BaseTemplate() {

    fun applyPom(project: Project): String {
        return project.applyTemplate(SPONGE_POM_TEMPLATE, BasicMavenStep.pluginVersions)
    }

    fun applySubPom(project: Project): String {
        return project.applyTemplate(SPONGE_SUBMODULE_POM_TEMPLATE, BasicMavenStep.pluginVersions)
    }

    fun applyMainClass(project: Project, buildSystem: BuildSystem, config: SpongeProjectConfig): String {
        val props = mutableMapOf("build" to buildSystem, "config" to config)
        return project.applyTemplate(SPONGE_MAIN_CLASS_TEMPLATE, props)
    }

    fun applyBuildGradle(project: Project, buildSystem: BuildSystem, config: SpongeProjectConfig): String {
        val props = mutableMapOf("build" to buildSystem, "config" to config)
        return project.applyTemplate(SPONGE_BUILD_GRADLE_TEMPLATE, props)
    }

    fun applyGradleProp(project: Project): String {
        return project.applyTemplate(SPONGE_GRADLE_PROPERTIES_TEMPLATE)
    }

    fun applySettingsGradle(project: Project, buildSystem: BuildSystem, config: SpongeProjectConfig): String {
        val props = mapOf("build" to buildSystem, "config" to config)
        return project.applyTemplate(SPONGE_SETTINGS_GRADLE_TEMPLATE, props)
    }

    fun applySubBuildGradle(project: Project, buildSystem: BuildSystem, config: SpongeProjectConfig): String {
        val props = mapOf("build" to buildSystem, "config" to config)
        return project.applyTemplate(SPONGE_SUBMODULE_BUILD_GRADLE_TEMPLATE, props)
    }
}
