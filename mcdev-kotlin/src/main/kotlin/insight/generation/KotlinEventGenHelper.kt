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

package com.demonwav.mcdev.kotlin.insight.generation

import com.demonwav.mcdev.insight.generation.EventGenHelper
import com.demonwav.mcdev.insight.generation.JvmEventGenHelper
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import org.jetbrains.kotlin.idea.core.ShortenReferences
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory

class KotlinEventGenHelper : EventGenHelper {

    private fun hasSuperType(ktClass: KtClassOrObject, fqn: String): Boolean {
        val names = setOf(fqn, fqn.substringAfterLast('.'))
        return ktClass.superTypeListEntries.any { it.text in names }
    }

    override fun addImplements(context: PsiElement, fqn: String) {
        val ktClass = context.parentOfType<KtClassOrObject>(true) ?: return
        if (hasSuperType(ktClass, fqn)) {
            return
        }

        val factory = KtPsiFactory.contextual(context)
        val entry = factory.createSuperTypeEntry(fqn)
        val insertedEntry = ktClass.addSuperTypeListEntry(entry)
        ShortenReferences.DEFAULT.process(insertedEntry)
    }

    override fun reformatAndShortenRefs(file: PsiFile, startOffset: Int, endOffset: Int) {
        file as? KtFile ?: return
        val project = file.project

        val marker = JvmEventGenHelper.doReformat(project, file, startOffset, endOffset) ?: return

        ShortenReferences.DEFAULT.process(file, marker.startOffset, marker.endOffset)
    }
}
