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
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("mcdev-common")
}

val testLibs: Configuration by configurations.creating {
    isTransitive = false
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        content {
            includeGroup("org.spigotmc")
        }
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots/") {
        content {
            includeGroup("net.md-5")
        }
    }
}

val libs = the<LibrariesForLibs>()
dependencies {
    intellijPlatform {
        testFramework(TestFrameworkType.JUnit5)
        testFramework(TestFrameworkType.Plugin.Java)
    }

    testLibs(libs.test.mockJdk)
    testLibs(libs.test.mixin)
    testLibs(libs.mixinExtras.common)
    testLibs(libs.test.spigotapi)
    testLibs(libs.test.bungeecord)
    testLibs(libs.test.spongeapi) {
        artifact {
            classifier = "shaded"
        }
    }
    testLibs(libs.test.fabricloader)
    testLibs(libs.test.nbt) {
        artifact {
            extension = "nbt"
        }
    }
    testLibs(project(":mixin-test-data"))
}

tasks.test {
    dependsOn(tasks.jar, testLibs)

    testLibs.resolvedConfiguration.resolvedArtifacts.forEach {
        systemProperty("testLibs.${it.name}", it.file.absolutePath)
    }
    systemProperty("NO_FS_ROOTS_ACCESS_CHECK", "true")
    systemProperty("java.awt.headless", "true")

    jvmArgs(
        "-Dsun.io.useCanonCaches=false",
        "-Dsun.io.useCanonPrefixCache=false",
    )
}
