/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev

import com.intellij.openapi.help.WebHelpProvider

class MinecraftDevWebHelpProvider : WebHelpProvider() {
    override fun getHelpPageUrl(helpTopicId: String): String {
        return "https://minecraftdev.org/docs/${helpTopicId.removePrefix(helpTopicPrefix)}.html"
    }
}
