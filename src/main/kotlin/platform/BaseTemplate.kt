/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform

import com.demonwav.mcdev.creator.buildsystem.BuildSystem
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.Project

abstract class BaseTemplate {

    protected fun Project.applyTemplate(
        templateName: String,
        properties: Map<String, *>? = null
    ): String {
        val manager = FileTemplateManager.getInstance(this)
        val template = manager.getJ2eeTemplate(templateName)

        val allProperties = manager.defaultProperties.toMutableMap()
        properties?.let { prop -> allProperties.putAll(prop) }

        (allProperties["build"] as? BuildSystem)?.let { allProperties["pluginId"] = (it.parent ?: it).artifactId }

        return template.getText(allProperties)
    }
}
