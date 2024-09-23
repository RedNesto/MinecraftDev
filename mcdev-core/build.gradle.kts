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

import org.gradle.internal.jvm.Jvm

plugins {
    groovy
    `java-test-fixtures`
    `mcdev-module`
    `mcdev-parsing`
    `mcdev-test`
}

group = "com.demonwav.mcdev"

val gradleToolingExtension: Configuration by configurations.creating

val gradleToolingExtensionSourceSet: SourceSet = sourceSets.create("gradle-tooling-extension") {
    configurations.named(compileOnlyConfigurationName) {
        extendsFrom(gradleToolingExtension)
    }
}
val gradleToolingExtensionJar = tasks.register<Jar>(gradleToolingExtensionSourceSet.jarTaskName) {
    from(gradleToolingExtensionSourceSet.output)
    archiveClassifier.set("gradle-tooling-extension")
    exclude("META-INF/plugin.xml")
}

sourceSets.named("testFixtures") {
    kotlin.srcDir("src/testFixtures/kotlin")
}

dependencies {
    // Add tools.jar for the JDI API
    implementation(files(Jvm.current().toolsJar))

    implementation(files(gradleToolingExtensionJar))

    implementation(libs.mixinExtras.expressions)

    implementation(libs.mappingIo)
    implementation(libs.bundles.asm)

    implementation(libs.bundles.fuel)

    implementation(libs.bundles.coroutines) {
        exclude(module = "kotlinx-coroutines-core-jvm")
    }

    intellijPlatform {
        intellijIdeaCommunity(libs.versions.intellij.ide)

        registerMcDevDependencies()

        pluginVerifier()
    }

    // For non-SNAPSHOT versions (unless Jetbrains fixes this...) find the version with:
    // afterEvaluate { println(intellij.ideaDependency.get().buildNumber.substring(intellij.type.get().length + 1)) }
    gradleToolingExtension(libs.groovy)
    gradleToolingExtension(libs.gradleToolingExtension)
    gradleToolingExtension(libs.annotations)

    testImplementation(libs.junit.api)
    testCompileOnly(libs.junit.vintage) // Hack to get tests to compile and run
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.processResources {
    for (lang in arrayOf("", "_en")) {
        from("src/main/resources/messages.MinecraftDevelopment_en_US.properties") {
            rename { "messages.MinecraftDevelopment$lang.properties" }
        }
    }
    // These templates aren't allowed to be in a directory structure in the output jar
    // But we have a lot of templates that would get real hard to deal with if we didn't have some structure
    // So this just flattens out the fileTemplates/j2ee directory in the jar, while still letting us have directories
    exclude("fileTemplates/j2ee/**")
    from(fileTree("src/main/resources/fileTemplates/j2ee").files) {
        eachFile {
            relativePath = RelativePath(true, "fileTemplates", "j2ee", this.name)
        }
    }
}

registerLexer("AtLexer", "com/demonwav/mcdev/platform/mcp/at/gen")
registerParser("AtParser", "com/demonwav/mcdev/platform/mcp/at/gen")

registerLexer("AwLexer", "com/demonwav/mcdev/platform/mcp/aw/gen")
registerParser("AwParser", "com/demonwav/mcdev/platform/mcp/aw/gen")

registerLexer("NbttLexer", "com/demonwav/mcdev/nbt/lang/gen")
registerParser("NbttParser", "com/demonwav/mcdev/nbt/lang/gen")

registerLexer("LangLexer", "com/demonwav/mcdev/translations/lang/gen")
registerParser("LangParser", "com/demonwav/mcdev/translations/lang/gen")

registerLexer("MEExpressionLexer", "com/demonwav/mcdev/platform/mixin/expression/gen")
registerParser("MEExpressionParser", "com/demonwav/mcdev/platform/mixin/expression/gen")

registerLexer("TranslationTemplateLexer", "com/demonwav/mcdev/translations/template/gen")
