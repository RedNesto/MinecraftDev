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

package com.demonwav.mcdev.creator.custom

import com.demonwav.mcdev.asset.MCDevBundle
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.DslConfigurableBase
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.dsl.builder.MutableProperty
import com.intellij.ui.dsl.builder.panel

class CreatorConfigurable : DslConfigurableBase(), Configurable {

    override fun getDisplayName(): @NlsContexts.ConfigurableName String? = MCDevBundle("minecraft.settings.creator")

    override fun createPanel(): DialogPanel = panel {
        val settings = CreatorSettings.instance

        row(MCDevBundle("minecraft.settings.creator.repos")) {}

        row {
            templateRepoTable(
                MutableProperty(
                    { settings.creatorTemplateRepos.toMutableList() },
                    { settings.creatorTemplateRepos = it }
                )
            )
        }
    }
}
