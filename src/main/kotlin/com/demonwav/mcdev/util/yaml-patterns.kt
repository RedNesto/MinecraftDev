/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2019 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.util

import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

fun PsiElementPattern.Capture<out YAMLKeyValue>.hasKey(key: String) = with(
    object : PatternCondition<YAMLKeyValue>("hasKey") {
        override fun accepts(t: YAMLKeyValue, context: ProcessingContext?): Boolean = t.keyText == key
    }
)

fun yamlValueByKey(key: String): PsiElementPattern.Capture<YAMLScalar> =
    psiElement(YAMLScalar::class.java).inside(psiElement(YAMLKeyValue::class.java).hasKey(key))

fun yamlFileNamed(name: String) = PlatformPatterns.psiFile(YAMLFile::class.java).withName(name)
