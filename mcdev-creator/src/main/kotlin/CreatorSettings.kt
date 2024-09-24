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

import com.demonwav.mcdev.MinecraftSettings
import com.demonwav.mcdev.asset.MCDevBundle
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.application
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.Text

@State(name = "MinecraftCreatorSettings", storages = [Storage("minecraft_dev.xml")])
class CreatorSettings : PersistentStateComponent<CreatorSettings.State> {

    data class State(
        @Suppress("DEPRECATION") // This is for migrating repos from MinecraftSettings
        var creatorTemplateRepos: List<TemplateRepo> = MinecraftSettings.instance.state.creatorTemplateRepos.map {
            TemplateRepo(it.name, it.provider, it.data)
        },
    )

    @Tag("repo")
    data class TemplateRepo(
        @get:Attribute("name")
        var name: String,
        @get:Attribute("provider")
        var provider: String,
        @get:Text
        var data: String
    ) {
        constructor() : this("", "", "")

        companion object {

            fun makeBuiltinRepo(): TemplateRepo {
                return TemplateRepo(MCDevBundle("minecraft.settings.creator.repo.builtin_name"), "builtin", "true")
            }
        }
    }

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
        if (state.creatorTemplateRepos.isEmpty()) {
            state.creatorTemplateRepos = listOf()
        }
    }

    var creatorTemplateRepos: List<TemplateRepo>
        get() = state.creatorTemplateRepos.map { it.copy() }
        set(creatorTemplateRepos) {
            state.creatorTemplateRepos = creatorTemplateRepos.map { it.copy() }
        }

    companion object {

        val instance: CreatorSettings
            get() = application.service()
    }
}
