/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2018 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.sponge.util

import java.util.regex.Pattern

object SpongeConstants {

    const val PLUGIN_ANNOTATION = "org.spongepowered.api.plugin.Plugin"
    const val DEPENDENCY_ANNOTATION = "org.spongepowered.api.plugin.Dependency"
    const val TEXT_COLORS = "org.spongepowered.api.text.format.TextColors"
    const val LISTENER_ANNOTATION = "org.spongepowered.api.event.Listener"
    const val IS_CANCELLED_ANNOTATION = "org.spongepowered.api.event.filter.IsCancelled"
    const val CANCELLABLE = "org.spongepowered.api.event.Cancellable"
    const val EVENT_ISCANCELLED_METHOD_NAME = "isCancelled"
    const val DEFAULT_CONFIG_ANNOTATION = "org.spongepowered.api.config.DefaultConfig"
    const val CONFIG_DIR_ANNOTATION = "org.spongepowered.api.config.ConfigDir"
    const val INJECT_ANNOTATION = "com.google.inject.Inject"

    // Taken from https://github.com/SpongePowered/plugin-meta/blob/185f5c2/src/main/java/org/spongepowered/plugin/meta/PluginMetadata.java#L60
    val ID_PATTERN_STRING = "^[a-z][a-z0-9-_]{1,63}$"
    val ID_PATTERN = Pattern.compile(ID_PATTERN_STRING)
}
