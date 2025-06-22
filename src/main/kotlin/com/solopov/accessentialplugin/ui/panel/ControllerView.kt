package com.solopov.accessentialplugin.ui.panel

import com.solopov.accessentialplugin.android_debug_bridge.AndroidDebugBridgeScript
import com.solopov.accessentialplugin.controller.Controller
import com.intellij.ui.components.JBScrollPane
import javax.swing.JPanel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.border.EmptyBorder

abstract class ControllerView(private val controller: Controller) {
    protected fun AndroidDebugBridgeScript.executeAdbCommand() {
        CoroutineScope(Job() + Dispatchers.IO).launch {
            controller.runOnAllValidSelectedDevices { device -> device.executeScript(this@executeAdbCommand.toShellCommand()) }
        }
    }

    abstract fun createPanelContent(): Component

    fun createScrollablePanel(): Component {
        return JBScrollPane(
            JPanel(BorderLayout()).apply {
                border = EmptyBorder(8, 8, 8, 8) // Add some padding
                add(createPanelContent(), BorderLayout.NORTH)
            }
        ).apply {
            isOpaque = true
            autoscrolls = true
            border = EmptyBorder(0, 0, 0, 0) // Remove default border
            viewport.isOpaque = false
        }
    }
}