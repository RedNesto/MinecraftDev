package com.demonwav.mcdev.creator

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.JavaSdkVersion
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ui.configuration.JdkComboBox
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel
import com.intellij.ui.components.Label
import com.intellij.ui.layout.panel
import javax.swing.JComponent
import javax.swing.JPanel

class JavaVersionValidatorWizardStep(
    val creator: MinecraftProjectCreator,
    val context: WizardContext
) : ModuleWizardStep() {

    private var selectedJdk: Sdk? = null

    fun highestJDKVersionRequired(): JavaSdkVersion? {
        val highestJavaVersionRequired = creator.configs.maxOfOrNull { it.javaVersion } ?: return null
        return JavaSdkVersion.fromJavaVersion(highestJavaVersionRequired)
    }

    override fun isStepVisible(): Boolean {
        val projectJdk = context.projectJdk ?: return true
        val requiredJdkVersion = highestJDKVersionRequired() ?: return false
        return !JavaSdk.getInstance().isOfVersionOrHigher(projectJdk, requiredJdkVersion)
    }

    override fun getComponent(): JComponent {
        val requiredJdkVersion = highestJDKVersionRequired() ?: return JPanel()
        return panel {
            val comboBox = JdkComboBox(
                context.project,
                ProjectSdksModel().apply { reset(context.project) },
                null,
                { JavaSdk.getInstance().isOfVersionOrHigher(it, requiredJdkVersion) },
                { JavaSdkVersion.fromVersionString(it.version)?.isAtLeast(requiredJdkVersion) ?: false },
                null,
                { selectedJdk = it }
            )
            if (comboBox.itemCount > 0) {
                comboBox.selectedIndex = 0
                selectedJdk = comboBox.selectedJdk
            }
            row(Label("<html><font size=\"5\">Unmet JDK requirement</size></html>")) {}
            row("Project requires at least Java " + requiredJdkVersion.description + ":") {
                component(comboBox).constraints(grow)
            }
        }
    }

    override fun updateDataModel() {
        if (selectedJdk != null) {
            context.projectJdk = selectedJdk
        }
    }

    override fun validate(): Boolean {
        val requiredVersion = highestJDKVersionRequired() ?: return true
        val selectedJdkVersion = selectedJdk?.versionString?.let(JavaSdkVersion::fromVersionString) ?: return false
        return selectedJdkVersion.isAtLeast(requiredVersion)
    }
}
