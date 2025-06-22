package com.solopov.accessentialplugin.ui.panel

import com.solopov.accessentialplugin.controller.Controller
import com.solopov.accessentialplugin.utils.getString
import com.solopov.accessentialplugin.ui.component.TabbedPanel
import com.solopov.accessentialplugin.ui.contract.MainPlugin
import javax.swing.JSplitPane

class AccessentialMainView(controller: Controller) : MainPlugin {

    private val deviceView by lazy {
        AvailableDevicesView(controller).create()
    }

    private val mainContentPane: JSplitPane
        get() = JSplitPane(JSplitPane.HORIZONTAL_SPLIT).also {
            @Suppress("MagicNumber")
            it.resizeWeight = 0.3
            it.leftComponent = deviceView
            it.rightComponent = tabbedInterface
        }

    private val tabbedInterface by lazy {
        val tabs = mapOf(
            getString("panel.display.title") to DisplaySettingsView(controller).createPanelContent(),
            getString("panel.debug.title") to DebugView(controller).createPanelContent(),
            getString("panel.settings.title") to AccessibilitySettingsView(controller).createPanelContent(),
            getString("panel.talkback.title") to AssistiveView(controller).createPanelContent(),
        )
        TabbedPanel(tabs).create()
    }

    override fun build(completionHandler: (content: Any) -> Unit) {
        completionHandler(mainContentPane)
    }
}