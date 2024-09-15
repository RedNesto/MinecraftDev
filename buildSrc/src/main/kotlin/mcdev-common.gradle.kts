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

import org.cadixdev.gradle.licenser.header.HeaderStyle
import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform.base")
    id("org.cadixdev.licenser")
}

val javaTarget = 17

repositories {
    maven("https://repo.denwav.dev/repository/maven-public/")
    mavenCentral()

    maven("https://maven.fabricmc.net/") {
        content {
            includeModule("net.fabricmc", "mapping-io")
            includeModule("net.fabricmc", "fabric-loader")
        }
    }
    maven("https://repo.spongepowered.org/maven/") {
        content {
            includeGroup("org.spongepowered")
        }
    }

    intellijPlatform {
        defaultRepositories()
    }
}

val libs = the<LibrariesForLibs>()
dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)

    intellijPlatform {
        intellijIdeaCommunity(libs.versions.intellij.ide)

        testFramework(TestFrameworkType.JUnit5)
    }

    testImplementation(libs.junit.api)
    testCompileOnly(libs.junit.vintage) // Hack to get tests to compile and run
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaTarget))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs = listOf("-proc:none")
    options.release.set(javaTarget)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(javaTarget))
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(javaTarget.toString())
        languageVersion = KotlinVersion.KOTLIN_2_0
        freeCompilerArgs = listOf("-Xjvm-default=all", "-Xjdk-release=$javaTarget")
        optIn.add("kotlin.contracts.ExperimentalContracts")
    }
    kotlinDaemonJvmArguments.add("-Xmx2G")
}

intellijPlatform {
    instrumentCode = false
    buildSearchableOptions = false
}

license {
    header.set(resources.text.fromFile(rootProject.layout.projectDirectory.file("copyright.txt")))
    style["flex"] = HeaderStyle.BLOCK_COMMENT.format
    style["bnf"] = HeaderStyle.BLOCK_COMMENT.format

    val endings = listOf("java", "kt", "kts", "groovy", "gradle.kts", "xml", "properties", "html", "flex", "bnf")
    include(endings.map { "**/*.$it" })

    val projectDir = layout.projectDirectory.asFile
    exclude {
        it.file.toRelativeString(projectDir)
            .replace("\\", "/")
            .startsWith("src/test/resources")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.register("format") {
    group = "minecraft"
    description = "Formats source code according to project style"
    dependsOn(tasks.licenseFormat)
}
