/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.velocity.creator

import com.demonwav.mcdev.creator.buildsystem.BuildSystem
import com.demonwav.mcdev.creator.buildsystem.maven.BasicMavenStep
import com.demonwav.mcdev.platform.BaseTemplate
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.VELOCITY_BUILD_GRADLE_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.VELOCITY_GRADLE_PROPERTIES_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.VELOCITY_MAIN_CLASS_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.VELOCITY_MAIN_CLASS_V2_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.VELOCITY_POM_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.VELOCITY_SETTINGS_GRADLE_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.VELOCITY_SUBMODULE_BUILD_GRADLE_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.VELOCITY_SUBMODULE_POM_TEMPLATE
import com.demonwav.mcdev.util.SemanticVersion
import com.intellij.openapi.project.Project

object VelocityTemplate : BaseTemplate() {

    private val VELOCITY_2_SNAPSHOT = SemanticVersion.parse("2.0.0-SNAPSHOT")

    fun applyPom(project: Project): String =
        project.applyTemplate(VELOCITY_POM_TEMPLATE, BasicMavenStep.pluginVersions)

    fun applySubPom(project: Project): String =
        project.applyTemplate(VELOCITY_SUBMODULE_POM_TEMPLATE, BasicMavenStep.pluginVersions)

    fun applyMainClass(
        project: Project,
        buildSystem: BuildSystem,
        config: VelocityProjectConfig,
        version: SemanticVersion
    ): String {
        val template = if (version < VELOCITY_2_SNAPSHOT) {
            VELOCITY_MAIN_CLASS_TEMPLATE
        } else {
            VELOCITY_MAIN_CLASS_V2_TEMPLATE
        }

        val props = mapOf("build" to buildSystem, "config" to config)
        return project.applyTemplate(template, props)
    }

    fun applyBuildGradle(project: Project, buildSystem: BuildSystem, config: VelocityProjectConfig): String {
        val props = mapOf("build" to buildSystem, "config" to config)
        return project.applyTemplate(VELOCITY_BUILD_GRADLE_TEMPLATE, props)
    }

    fun applyGradleProp(project: Project): String = project.applyTemplate(VELOCITY_GRADLE_PROPERTIES_TEMPLATE)

    fun applySettingsGradle(project: Project, buildSystem: BuildSystem, config: VelocityProjectConfig): String {
        val props = mapOf("build" to buildSystem, "config" to config)
        return project.applyTemplate(VELOCITY_SETTINGS_GRADLE_TEMPLATE, props)
    }

    fun applySubBuildGradle(project: Project, buildSystem: BuildSystem, config: VelocityProjectConfig): String {
        val props = mapOf("build" to buildSystem, "config" to config)
        return project.applyTemplate(VELOCITY_SUBMODULE_BUILD_GRADLE_TEMPLATE, props)
    }
}
