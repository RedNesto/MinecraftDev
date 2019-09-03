/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2019 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.sponge.inspection.suppress

import com.demonwav.mcdev.platform.sponge.util.SpongePatterns
import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.psi.PsiElement
import com.intellij.spellchecker.inspections.SpellCheckingInspection

class SpongeSpellcheckerSuppressor : InspectionSuppressor {

    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        if (toolId != SpellCheckingInspection.SPELL_CHECKING_INSPECTION_TOOL_NAME) {
            return false
        }

        return SpongePatterns.PLUGIN_ID_LITERALS.accepts(element) ||
            SpongePatterns.PLUGIN_ANNOTATION_AUTHORS.accepts(element)
    }

    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> =
        SuppressQuickFix.EMPTY_ARRAY
}
