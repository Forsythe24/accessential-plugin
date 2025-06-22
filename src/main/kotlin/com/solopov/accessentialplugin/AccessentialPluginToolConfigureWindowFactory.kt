package com.solopov.accessentialplugin

import com.solopov.accessentialplugin.controller.AndroidStudioPluginController
import com.solopov.accessentialplugin.controller.Controller
import com.solopov.accessentialplugin.ui.contract.MainPlugin
import com.solopov.accessentialplugin.ui.panel.AccessentialMainView
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import javax.swing.JComponent

class AccessentialPluginToolConfigureWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val mainPanel: MainPlugin = AccessentialMainView(AndroidStudioPluginController(project))

        mainPanel.build { content ->
            toolWindow.contentManager.addContent(ContentFactory.getInstance().createContent(content as JComponent, "", false))
        }
    }
}