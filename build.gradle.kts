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

import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.tasks.PrepareSandboxTask

plugins {
    id(libs.plugins.changelog.get().pluginId)
    id(libs.plugins.idea.ext.get().pluginId)
    `mcdev-core`
    `mcdev-publishing`
}

val coreVersion: String by project

group = "com.demonwav.mcdev"

val templatesSourceSet: SourceSet = sourceSets.create("templates") {
    resources {
        srcDir("templates")
    }
}

val templateSourceSets: List<SourceSet> = (file("templates").listFiles() ?: emptyArray()).mapNotNull { file ->
    if (file.isDirectory() && (file.listFiles() ?: emptyArray()).any { it.name.endsWith(".mcdev.template.json") }) {
        sourceSets.create("templates-${file.name}") {
            resources {
                srcDir(file)
            }
        }
    } else {
        null
    }
}

val externalAnnotationsJar = tasks.register<Jar>("externalAnnotationsJar") {
    from("externalAnnotations")
    destinationDirectory.set(layout.buildDirectory.dir("externalAnnotations"))
    archiveFileName.set("externalAnnotations.jar")
}

dependencies {
    for (templateSourceSet in (templateSourceSets + templatesSourceSet)) {
        templateSourceSet.implementationConfigurationName(projects.mcdevCore) {
            isTransitive = false
        }
    }

    intellijPlatform {
        intellijIdeaCommunity(libs.versions.intellij.ide)

        registerMcDevDependencies()

        bundledPlugin("org.jetbrains.kotlin")
        bundledPlugin("org.toml.lang")
        bundledPlugin("org.jetbrains.plugins.yaml")

        pluginModule(implementation(projects.mcdevCore))
        pluginModule(implementation(projects.mcdevKotlin))
        pluginModule(implementation(projects.mcdevToml))
        pluginModule(implementation(projects.mcdevYaml))

        pluginVerifier()
    }
}

changelog {
    version = coreVersion
    groups.empty()
    path = "changelog.md"
}

intellijPlatform {
    projectName = "Minecraft Development"

    pluginVerification {
        ides {
            recommended()
        }
    }
}

tasks.patchPluginXml {
    val changelog = project.changelog
    changeNotes = changelog.render(Changelog.OutputType.HTML)
}

license {
    val endings = listOf("java", "kt", "kts", "groovy", "gradle.kts", "xml", "properties", "html", "flex", "bnf")
    include(endings.map { "**/*.$it" })

    val projectDir = layout.projectDirectory.asFile
    exclude {
        it.file.toRelativeString(projectDir)
            .replace("\\", "/")
            .startsWith("src/test/resources")
    }

    tasks {
        register("gradle") {
            files.from(
                fileTree(project.projectDir) {
                    include("**/*.gradle.kts", "**/gradle.properties")
                    exclude("**/buildSrc/**", "**/build/**", "**/.sandbox/**")
                },
            )
        }
        register("buildSrc") {
            files.from(
                project.fileTree(project.projectDir.resolve("buildSrc")) {
                    include("**/*.kt", "**/*.kts")
                    exclude("**/build/**")
                },
            )
        }
        register("mixinTestData") {
            files.from(
                project.fileTree(project.projectDir.resolve("mixin-test-data")) {
                    include("**/*.java", "**/*.kts")
                    exclude("**/build/**")
                },
            )
        }
        register("externalAnnotations") {
            files.from(project.fileTree("externalAnnotations"))
        }
    }
}

tasks.withType<PrepareSandboxTask> {
    from(externalAnnotationsJar) {
        into("Minecraft Development/lib/resources")
    }
    from("templates") {
        exclude(".git")
        into("Minecraft Development/lib/resources/builtin-templates")
    }
}

tasks.runIde {
    maxHeapSize = "4G"

    System.getProperty("debug")?.let {
        systemProperty("idea.ProcessCanceledException", "disabled")
        systemProperty("idea.debug.mode", "true")
    }
    // Set these properties to test different languages
    // systemProperty("user.language", "fr")
    // systemProperty("user.country", "FR")
}
