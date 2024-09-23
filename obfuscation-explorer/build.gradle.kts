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

import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    `mcdev-core`
    `mcdev-parsing`
    `mcdev-publishing`
}

val jflex by configurations
val jflexSkeleton by configurations
val grammarKit by configurations

group = "io.mcdev.obfex"

intellijPlatform {
    projectName = "Obfuscation Explorer"
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity(libs.versions.intellij.ide)

        plugin(libs.versions.psiPlugin.map { "PsiViewer:$it" })

        testFramework(TestFrameworkType.JUnit5)
        testFramework(TestFrameworkType.Platform)
    }
}

registerLexer("SrgLexer", "io/mcdev/obfex/formats/srg/gen")
registerParser("SrgParser", "io/mcdev/obfex/formats/srg/gen")

registerLexer("CSrgLexer", "io/mcdev/obfex/formats/csrg/gen")
registerParser("CSrgParser", "io/mcdev/obfex/formats/csrg/gen")

registerLexer("TSrgLexer", "io/mcdev/obfex/formats/tsrg/gen")
registerParser("TSrgParser", "io/mcdev/obfex/formats/tsrg/gen")

registerLexer("TSrg2Lexer", "io/mcdev/obfex/formats/tsrg2/gen")
registerParser("TSrg2Parser", "io/mcdev/obfex/formats/tsrg2/gen")

registerLexer("JamLexer", "io/mcdev/obfex/formats/jam/gen")
registerParser("JamParser", "io/mcdev/obfex/formats/jam/gen")

registerLexer("EnigmaLexer", "io/mcdev/obfex/formats/enigma/gen")
registerParser("EnigmaParser", "io/mcdev/obfex/formats/enigma/gen")

registerLexer("TinyV1Lexer", "io/mcdev/obfex/formats/tinyv1/gen")
registerParser("TinyV1Parser", "io/mcdev/obfex/formats/tinyv1/gen")

registerLexer("TinyV2Lexer", "io/mcdev/obfex/formats/tinyv2/gen")
registerParser("TinyV2Parser", "io/mcdev/obfex/formats/tinyv2/gen")

registerLexer("ProGuardLexer", "io/mcdev/obfex/formats/proguard/gen")
registerParser("ProGuardParser", "io/mcdev/obfex/formats/proguard/gen")
