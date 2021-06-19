/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.creator

import com.demonwav.mcdev.asset.MCDevBundle
import com.demonwav.mcdev.creator.buildsystem.BuildSystem
import com.demonwav.mcdev.creator.buildsystem.BuildSystemType
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.externalSystem.util.ExternalSystemBundle
import com.intellij.openapi.observable.properties.GraphPropertyImpl.Companion.graphProperty
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.panel
import javax.swing.DefaultComboBoxModel
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener

class BuildSystemWizardStep(
    private val context: WizardContext,
    private val creator: MinecraftProjectCreator
) : ModuleWizardStep() {

    private val buildSystemModel = DefaultComboBoxModel(BuildSystemType.values())

    private val properties = PropertyGraph()
    private val groupIdProperty = properties.graphProperty { "" }
    private val artifactIdProperty = properties.graphProperty { "" }
    private val versionProperty = properties.graphProperty { "1.0-SNAPSHOT" }
    private val buildSystemProperty = properties.graphProperty { buildSystemModel.getElementAt(0)!! }

    private var groupId by groupIdProperty
    private var artifactId by artifactIdProperty
    private var version by versionProperty
    private var buildSystem by buildSystemProperty

    private val contentPanel: DialogPanel by lazy {
        panel {
            row("<html><font size=\"5\">Build Settings</font></html>") {
                largeGapAfter()
            }
            row(ExternalSystemBundle.message("external.system.mavenized.structure.wizard.group.id.label")) {
                textField(groupIdProperty)
                    .withValidationOnApply { validateGroupId() }
                    .withValidationOnInput { validateGroupId() }
                    .comment(MCDevBundle.message("creator.groupid.description"), 120)
                    .focused()
            }
            row(ExternalSystemBundle.message("external.system.mavenized.structure.wizard.artifact.id.label")) {
                textField(artifactIdProperty)
                    .withValidationOnApply { validateArtifactId() }
                    .withValidationOnInput { validateArtifactId() }
                    .comment(
                        ExternalSystemBundle.message(
                            "external.system.mavenized.structure.wizard.artifact.id.help",
                            context.presentationName
                        )
                    )
            }
            row(ExternalSystemBundle.message("external.system.mavenized.structure.wizard.version.label")) {
                textField(versionProperty)
                    .withValidationOnApply { validateVersion() }
                    .withValidationOnInput { validateVersion() }
            }
            row {
                right {
                    val buildSystemBox = comboBox(buildSystemModel, buildSystemProperty)
                    buildSystemModel.addListDataListener(
                        object : ListDataListener {
                            override fun intervalAdded(e: ListDataEvent?) = update()

                            override fun intervalRemoved(e: ListDataEvent?) = update()

                            override fun contentsChanged(e: ListDataEvent?) = update()

                            private fun update() {
                                buildSystemBox.enabled(buildSystemModel.size > 1)
                            }
                        }
                    )
                }
            }
        }.apply { registerValidators(context.disposable) }
    }

    override fun getPreferredFocusedComponent() = contentPanel.preferredFocusedComponent

    override fun getComponent() = contentPanel

    override fun getHelpId(): String = "com.demonwav.minecraft-dev.create"

    override fun updateStep() {
        buildSystemModel.removeAllElements()

        val types = BuildSystemType.values().filter { type ->
            creator.configs.all { type.creatorType.isInstance(it) }
        }

        buildSystemModel.addAll(types)

        // We prefer Gradle, so if it's included, choose it
        // If Gradle is not included, luck of the draw
        if (creator.configs.any { it.preferredBuildSystem == BuildSystemType.GRADLE }) {
            buildSystemModel.selectedItem = BuildSystemType.GRADLE
            return
        }

        val counts = creator.configs.asSequence()
            .mapNotNull { it.preferredBuildSystem }
            .groupingBy { it }
            .eachCount()

        val maxValue = counts.maxByOrNull { it.value }?.value ?: return
        counts.asSequence()
            .filter { it.value == maxValue }
            .map { it.key }
            .firstOrNull()
            ?.let { mostPopularType ->
                buildSystemModel.selectedItem = mostPopularType
            }
    }

    override fun updateDataModel() {
        creator.buildSystem = createBuildSystem()
    }

    private fun createBuildSystem(): BuildSystem {
        return buildSystem.create(groupId, artifactId, version)
    }

    override fun validate(): Boolean {
        return contentPanel.validateCallbacks
            .asSequence()
            .mapNotNull { it() }
            .all { it.okEnabled }
    }

    private fun ValidationInfoBuilder.validateGroupId(): ValidationInfo? {
        if (groupId.isBlank()) {
            return error("Group ID must not be blank")
        }
        return null
    }

    private fun ValidationInfoBuilder.validateArtifactId(): ValidationInfo? {
        if (artifactId.isBlank()) {
            return error("Artifact ID must not be blank")
        }
        return null
    }

    private fun ValidationInfoBuilder.validateVersion(): ValidationInfo? {
        if (version.isEmpty()) {
            return error("Version must not be empty")
        }
        return null
    }
}
