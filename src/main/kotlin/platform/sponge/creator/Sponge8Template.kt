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
import com.demonwav.mcdev.platform.BaseTemplate
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.SPONGE8_BUILD_GRADLE_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.SPONGE8_GRADLE_PROPERTIES_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.SPONGE8_MAIN_CLASS_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.SPONGE8_PLUGINS_JSON_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.SPONGE8_SETTINGS_GRADLE_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.SPONGE8_SUBMODULE_BUILD_GRADLE_TEMPLATE
import com.intellij.openapi.project.Project

object Sponge8Template : BaseTemplate() {

    fun applyMainClass(project: Project, buildSystem: BuildSystem, config: SpongeProjectConfig): String {
        val props = mutableMapOf("build" to buildSystem, "config" to config)
        return project.applyTemplate(SPONGE8_MAIN_CLASS_TEMPLATE, props)
    }

    fun applyPluginsJson(project: Project, buildSystem: BuildSystem, config: SpongeProjectConfig): String {
        val props = mutableMapOf("build" to buildSystem, "config" to config)
        return project.applyTemplate(SPONGE8_PLUGINS_JSON_TEMPLATE, props)
    }

    fun applyBuildGradle(project: Project, buildSystem: BuildSystem, config: SpongeProjectConfig): String {
        val props = mutableMapOf("build" to buildSystem, "config" to config)
        return project.applyTemplate(SPONGE8_BUILD_GRADLE_TEMPLATE, props)
    }

    fun applyGradleProp(project: Project): String {
        return project.applyTemplate(SPONGE8_GRADLE_PROPERTIES_TEMPLATE)
    }

    fun applySettingsGradle(project: Project, buildSystem: BuildSystem, config: SpongeProjectConfig): String {
        val props = mutableMapOf("build" to buildSystem, "config" to config)
        return project.applyTemplate(SPONGE8_SETTINGS_GRADLE_TEMPLATE, props)
    }

    fun applySubBuildGradle(project: Project, buildSystem: BuildSystem, config: SpongeProjectConfig): String {
        val props = mutableMapOf("build" to buildSystem, "config" to config)
        return project.applyTemplate(SPONGE8_SUBMODULE_BUILD_GRADLE_TEMPLATE, props)
    }
}
