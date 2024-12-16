package com.demonwav.mcdev.platform.mcp.aw

import com.demonwav.mcdev.platform.mcp.aw.AwElementFactory.Access.entries
import com.demonwav.mcdev.platform.mcp.aw.gen.psi.AwEntry
import com.demonwav.mcdev.platform.mcp.aw.gen.psi.AwHeader
import com.demonwav.mcdev.platform.mcp.aw.gen.psi.AwTypes
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiFileFactory

object AwElementFactory {

    fun createFile(project: Project, text: String): AwFile {
        return PsiFileFactory.getInstance(project).createFileFromText("name", AwFileType, text) as AwFile
    }

    fun createEntry(project: Project, entry: String): AwEntry {
        val file = createFile(project, entry)
        return file.firstChild as AwEntry
    }

    fun createComment(project: Project, comment: String): PsiComment {
        val line = "# $comment"
        val file = createFile(project, line)

        return file.node.findChildByType(AwTypes.COMMENT)!!.psi as PsiComment
    }

    fun createHeader(project: Project): AwHeader {
        val file = createFile(project, "accessWidener v2 named\n")
        return file.firstChild as AwHeader
    }

    enum class Access(val text: String) {
        EXTENDABLE("extendable"),
        ACCESSIBLE("accessible"),
        MUTABLE("mutable"),
        TRANSITIVE_EXTENDABLE("transitive-extendable"),
        TRANSITIVE_ACCESSIBLE("transitive-accessible"),
        TRANSITIVE_MUTABLE("transitive-mutable"),
        ;

        companion object {
            fun match(s: String) = entries.firstOrNull { it.text == s }
            fun softMatch(s: String) = entries.filter { it.text.contains(s) }
        }
    }
}
