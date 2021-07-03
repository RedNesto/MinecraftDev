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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.updateSettings.impl.UpdateSettings
import java.io.IOException

class ConfigurePluginUpdatesDialog : DialogWrapper(true) {

    private val form = ConfigurePluginUpdatesForm()
    private var update: PluginUpdateStatus.Update? = null
    private var initialSelectedChannel: Int = 0

    init {
        title = "Configure Minecraft Development Plugin Updates"
        form.channelsListingInProgressIcon.suspend()
        form.channelsListingInProgressIcon.setPaintPassiveIcon(false)
        form.updateCheckInProgressIcon.suspend()
        form.updateCheckInProgressIcon.setPaintPassiveIcon(false)

        form.channelBox.addItem("Stable")

        form.checkForUpdatesNowButton.addActionListener {
            saveSettings()
            form.updateCheckInProgressIcon.resume()
            resetUpdateStatus()
            PluginUpdater.runUpdateCheck { pluginUpdateStatus ->
                form.updateCheckInProgressIcon.suspend()

                form.updateStatusLabel.text = when (pluginUpdateStatus) {
                    is PluginUpdateStatus.LatestVersionInstalled ->
                        "You have the latest version of the plugin (${PluginUtil.pluginVersion}) installed."
                    is PluginUpdateStatus.Update -> {
                        update = pluginUpdateStatus
                        form.installButton.isVisible = true
                        "A new version (${pluginUpdateStatus.pluginDescriptor.version}) is available"
                    }
                    is PluginUpdateStatus.CheckFailed -> "Update check failed: " + pluginUpdateStatus.message
                }

                false
            }
        }

        form.installButton.isVisible = false
        form.installButton.addActionListener {
            update?.let { update ->
                close(OK_EXIT_CODE)
                try {
                    PluginUpdater.installPluginUpdate(update)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        form.channelBox.addActionListener { resetUpdateStatus() }

        form.channelsListingInProgressIcon.resume()
        ApplicationManager.getApplication().executeOnPooledThread {
            val otherChannels = Channel.otherChannels
            otherChannels.forEachIndexed { i, channel ->
                if (channel.hasChannel()) {
                    initialSelectedChannel = i + 1
                    return@forEachIndexed
                }
            }
            invokeLater(ModalityState.any()) {
                for (channels in otherChannels) {
                    form.channelBox.addItem(channels.title)
                }
                form.channelBox.isEnabled = true
                form.channelBox.selectedIndex = initialSelectedChannel
                form.channelsListingInProgressIcon.suspend()
            }
        }

        init()
    }

    override fun createCenterPanel() = form.panel

    private fun saveSelectedChannel(index: Int) {
        val hosts = UpdateSettings.getInstance().storedPluginHosts
        hosts.removeIf(Channel.urlRegex::matches)

        if (index != 0) {
            val channel = Channel.otherChannels[index - 1]
            hosts.add(channel.url)
        }
    }

    private fun saveSettings() {
        saveSelectedChannel(form.channelBox.selectedIndex)
    }

    private fun resetUpdateStatus() {
        form.updateStatusLabel.text = " "
        form.installButton.isVisible = false
    }

    override fun doOKAction() {
        saveSettings()
        super.doOKAction()
    }

    override fun doCancelAction() {
        saveSelectedChannel(initialSelectedChannel)
        super.doCancelAction()
    }
}
