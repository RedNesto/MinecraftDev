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

import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers

plugins {
    idea
    id("org.jetbrains.gradle.plugin.idea-ext")
    id("org.cadixdev.licenser")
}

val jflex: Configuration by configurations.creating
val jflexSkeleton: Configuration by configurations.creating
val grammarKit: Configuration by configurations.creating

val libs = the<LibrariesForLibs>()
dependencies {
    jflex(libs.jflex.lib)
    jflexSkeleton(libs.jflex.skeleton) {
        artifact {
            extension = "skeleton"
        }
    }
    grammarKit(libs.grammarKit)
}

val generate by tasks.registering {
    group = "minecraft"
    description = "Generates sources needed to compile the plugin."
    outputs.dir(layout.buildDirectory.dir("gen"))
}

the<SourceSetContainer>().named("main") {
    java.srcDir(generate)
}

// Remove gen directory on clean
tasks.named<Delete>("clean") { delete(generate) }

idea {
    module {
        generatedSourceDirs.add(file("build/gen"))
    }
}

rootProject.idea {
    project.settings.taskTriggers.afterSync(generate)
}

license {
    tasks {
        register("grammars") {
            files.from(project.fileTree("src/main/grammars"))
        }
    }
}
