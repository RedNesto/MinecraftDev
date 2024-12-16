package com.demonwav.mcdev.platform.mcp.aw.inspections

import com.demonwav.mcdev.platform.mcp.aw.AwFile
import com.demonwav.mcdev.platform.mcp.aw.fixes.CreateAwHeaderFix
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.Nls

class AwHeaderInspection : LocalInspectionTool() {

    @Nls
    override fun getStaticDescription(): String? = "Reports problems about Access Widener headers"

    override fun checkFile(
        file: PsiFile,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<out ProblemDescriptor>? {
        if ((file as? AwFile)?.header == null) {
            return arrayOf(
                manager.createProblemDescriptor(
                    file.firstChild ?: file,
                    "Missing header",
                    CreateAwHeaderFix(),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOnTheFly
                )
            )
        }

        return null
    }
}
