package me.mbolotov.json.schema.generator

import com.intellij.ide.projectView.impl.ProjectViewTree
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.json.JsonFileType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.io.readText
import com.intellij.util.io.write
import javax.swing.tree.DefaultMutableTreeNode

class GenerateSchemaAction : AnAction() {
    override fun update(e: AnActionEvent) {
        if (e.extractFile() == null) {
            e.presentation.isVisible = false
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val file = e.extractFile() ?: return
        val json = file.toNioPath().readText()
        val name = file.nameWithoutExtension
        val schema = JsonSchemaGenerator.outputAsString(name, "Auto generated JSON schema based on the '$name' file", json)

        val project = e.project ?: return

        runWriteAction {
            val psi = PsiFileFactory.getInstance(project).createFileFromText(
                file.parent.path + "/${name}Schema.json",
                JsonFileType.INSTANCE,
                schema
            )

            val output = VfsUtil.createChildSequent(e, file.parent, "${name}Schema", "json")
            val styleMan = CodeStyleManager.getInstance(project)
            val formatted = styleMan.reformat(psi)

            output.toNioPath().write(formatted.text)
            LocalFileSystem.getInstance().refreshAndFindFileByNioFile(output.toNioPath())
        }
    }
}

private fun AnActionEvent.extractFile(): VirtualFile? {
    return (((getData(PlatformDataKeys.CONTEXT_COMPONENT) as? ProjectViewTree)
        ?.selectionPath?.lastPathComponent as? DefaultMutableTreeNode)
        ?.userObject as? PsiFileNode)?.virtualFile
        ?.takeIf { it.extension == "json" }
}
