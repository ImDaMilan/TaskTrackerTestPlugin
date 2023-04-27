package com.imdamilan.testtask

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import java.io.File
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

        val virtualFiles = project.basePath?.let { basePath ->
            File(basePath).walkTopDown().filter { it.extension == "kt" }
        }
        virtualFiles?.forEach { it ->
            val node = DefaultMutableTreeNode(it.name)
            val fileContent = it.inputStream().use { it.bufferedReader().readText() }
            val classCount = "class".toRegex().findAll(fileContent).count()
            val functionCount = "fun ".toRegex().findAll(fileContent).count()
            node.add(DefaultMutableTreeNode("$classCount classes"))
            node.add(DefaultMutableTreeNode("$functionCount functions"))
            (treeModel.root as DefaultMutableTreeNode).add(node)
        }
        treeModel.reload()

        toolWindow.contentManager.addContentManagerListener(object : ContentManagerListener {
            override fun contentRemoved(event: ContentManagerEvent) {
                if (event.content.isCloseable) {
                    toolWindow.hide(null)
                }
            }
        })
    }

    override fun init(toolWindow: ToolWindow) {
        toolWindow.setAdditionalGearActions(DefaultActionGroup.EMPTY_GROUP)
    }
}
