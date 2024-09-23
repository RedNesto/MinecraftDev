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

plugins {
    idea
    id("mcdev-common")
    id("org.jetbrains.intellij.platform")
}

val ideaVersionName: String by project
val coreVersion: String by project
val buildNumber: String? by project

version = "$ideaVersionName-$coreVersion"

// Build numbers are used for nightlies
if (buildNumber != null) {
    version = "$version-$buildNumber"
}

intellijPlatform {
    sandboxContainer = layout.projectDirectory.dir(".sandbox")
}

idea {
    module {
        excludeDirs.add(file(intellijPlatform.sandboxContainer.get()))
    }
}

tasks.runIde {
    maxHeapSize = "2G"
    jvmArgs("--add-exports=java.base/jdk.internal.vm=ALL-UNNAMED")
}

tasks.register("cleanSandbox", Delete::class) {
    group = "intellij"
    description = "Deletes the sandbox directory."
    delete(layout.projectDirectory.dir(".sandbox"))
}
