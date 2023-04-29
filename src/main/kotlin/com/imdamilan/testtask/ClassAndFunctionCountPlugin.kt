package com.imdamilan.testtask

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFileEvent
import com.intellij.openapi.vfs.VirtualFileListener
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel


class ClassAndFunctionCountPlugin : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val panel = JPanel(BorderLayout())

        val treeModel = DefaultTreeModel(DefaultMutableTreeNode("Project Files"))
        val tree = Tree(treeModel)
        val scrollPane = JBScrollPane(tree)
        panel.add(scrollPane, BorderLayout.CENTER)

        toolWindow.contentManager.addContent(contentFactory.createContent(panel, "", false))

        val listener = object : VirtualFileListener {
            override fun beforeContentsChange(event: VirtualFileEvent) {
                if (event.file.extension.equals("java")) {
                    val psiFile = PsiManager.getInstance(project).findFile(event.file) ?: return
                    var classes = 0
                    var methods = 0
                    psiFile.accept(object : JavaRecursiveElementVisitor() {
                        override fun visitClass(aClass: PsiClass?) {
                            super.visitClass(aClass)
                            classes++
                        }

                        override fun visitMethod(method: PsiMethod?) {
                            super.visitMethod(method)
                            methods++
                        }
                    })
                    val root = treeModel.root as DefaultMutableTreeNode
                    val node = DefaultMutableTreeNode(event.fileName)
                    node.add(DefaultMutableTreeNode("Classes: $classes"))
                    node.add(DefaultMutableTreeNode("Methods: $methods"))
                    val children = root.children()
                    while (children.hasMoreElements()) {
                        val child = children.nextElement() as DefaultMutableTreeNode
                        if (child.userObject == node.userObject) {
                            treeModel.removeNodeFromParent(child)
                            break
                        }
                    }
                    treeModel.insertNodeInto(node, root, root.childCount)
                }
            }
        }

        LocalFileSystem.getInstance().addVirtualFileListener(listener)
    }

    override fun init(toolWindow: ToolWindow) {
        toolWindow.setAdditionalGearActions(DefaultActionGroup.EMPTY_GROUP)
    }
}
