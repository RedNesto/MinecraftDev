/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.update

import com.demonwav.mcdev.util.fromJson
import com.google.gson.Gson
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.updateSettings.impl.UpdateSettings
import com.intellij.openapi.util.text.StringUtil
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

data class Channel(val title: String) {

    val url: String = "https://plugins.jetbrains.com/plugins/$title/8327"

    fun hasChannel(): Boolean = UpdateSettings.getInstance().pluginHosts.contains(url)

    companion object {

        private val LOG = logger<Channel>()

        val urlRegex = "^https://plugins.jetbrains.com/plugins/(.+)/8327$".toRegex()

        val otherChannels by lazy {
            try {
                val channelsUri = URI.create("https://plugins.jetbrains.com/api/plugins/8327/channels")
                val request = HttpRequest.newBuilder(channelsUri).timeout(Duration.ofSeconds(2)).build()
                val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).body()
                Gson().fromJson<List<String>>(response)
                    .filterNot(String::isNullOrBlank)
                    .map(StringUtil::toTitleCase)
                    .map(::Channel)
            } catch (e: Exception) {
                LOG.error("Failed to fetch plugin channels", e)
                listOf(Channel("Nightly"))
            }
        }
    }
}
