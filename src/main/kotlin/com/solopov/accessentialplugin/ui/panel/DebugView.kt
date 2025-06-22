package com.solopov.accessentialplugin.ui.panel

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.solopov.accessentialplugin.android_debug_bridge.*
import com.solopov.accessentialplugin.android_debug_bridge.params.SettingsScreen
import com.solopov.accessentialplugin.controller.Controller
import com.solopov.accessentialplugin.utils.getString
import com.solopov.accessentialplugin.utils.createDropDownMenu
import com.solopov.accessentialplugin.utils.createToggleRow
import java.awt.GridBagLayout
import javax.swing.JPanel

class DebugView(private val controller: Controller) : ControllerView(controller) {

    private val labelScanner = getString("panel.debug.label.scanner")
    private val btnScannerOn = getString("panel.debug.button.scanner.on")
    private val btnScannerOff = getString("panel.debug.button.scanner.off")

    private val labelBounds = getString("panel.debug.label.bounds")
    private val btnBoundsOn = getString("panel.debug.button.bounds.on")
    private val btnBoundsOff = getString("panel.debug.button.bounds.off")

    private val labelTaps = getString("panel.debug.label.taps")
    private val btnTapsOn = getString("panel.debug.button.taps.on")
    private val btnTapsOff = getString("panel.debug.button.taps.off")

    private val labelScreen = getString("panel.debug.label.screen")
    private val screenChoices = SettingsScreen.values().map { it.reference }

    private val labelSwitchAccess = getString("panel.debug.label.switch_access")
    private val btnSwitchOn = getString("panel.debug.button.switch_access.on")
    private val btnSwitchOff = getString("panel.debug.button.switch_access.off")

    private val labelVoiceAccess = getString("panel.debug.label.voice_access")
    private val btnVoiceOn = getString("panel.debug.button.voice_access.on")
    private val btnVoiceOff = getString("panel.debug.button.voice_access.off")

    override fun createPanelContent(): JPanel = JPanel(GridBagLayout()).apply {
        with(this) {
            addDropdownRow(0, labelScreen, screenChoices) { choice ->
                SettingsScreen.values().find { it.reference == choice }?.let {
                    openSettingsScreen(it).executeAdbCommand()
                }
            }

            addToggleRow(1, labelBounds, btnBoundsOn, btnBoundsOff,
                onAction = { toggleLayoutBounds(true).executeAdbCommand() },
                offAction = { toggleLayoutBounds(false).executeAdbCommand() }
            )

            addToggleRow(2, labelTaps, btnTapsOn, btnTapsOff,
                onAction = { toggleTouchIndication(true).executeAdbCommand() },
                offAction = { toggleTouchIndication(false).executeAdbCommand() }
            )

            addToggleRow(3, labelScanner, btnScannerOn, btnScannerOff,
                onAction = { configureAccessibilityScanner(true).executeAdbCommand() },
                offAction = { configureAccessibilityScanner(false).executeAdbCommand() }
            )

            addToggleRow(4, labelVoiceAccess, btnVoiceOn, btnVoiceOff,
                onAction = {
                    showHelpNotification(
                        titleKey = "notification.voice.access.help.title",
                        messageKey = "notification.voice.access.help.message",
                        actionKey = "notification.voice.access.help.action"
                    )
                    configureVoiceAccess(true).executeAdbCommand()
                },
                offAction = { configureVoiceAccess(false).executeAdbCommand() }
            )

            addToggleRow(5, labelSwitchAccess, btnSwitchOn, btnSwitchOff,
                onAction = {
                    showHelpNotification(
                        titleKey = "notification.switch.access.help.title",
                        messageKey = "notification.switch.access.help.message",
                        actionKey = "notification.switch.access.help.action"
                    )
                    configureSwitchAccess(true).executeAdbCommand()
                },
                offAction = { configureSwitchAccess(false).executeAdbCommand() }
            )
        }
    }

    private fun JPanel.addDropdownRow(
        rowIndex: Int,
        label: String,
        options: List<String>,
        onSelect: (String) -> Unit
    ) {
        createDropDownMenu(label, rowIndex, options, onSelect)
    }

    private fun JPanel.addToggleRow(
        rowIndex: Int,
        label: String,
        onLabel: String,
        offLabel: String,
        onAction: () -> Unit,
        offAction: () -> Unit
    ) {
        createToggleRow(label = label, whichRow = rowIndex, positiveLabel = onLabel, negativeLabel = offLabel, positiveAction = onAction, negativeAction = offAction)
    }

    private fun showHelpNotification(titleKey: String, messageKey: String, actionKey: String) {
        controller.showNotification(
            title = getString(titleKey),
            message = getString(messageKey),
            type = NotificationType.INFORMATION,
            actions = listOf(
                object : NotificationAction(getString(actionKey)) {
                    override fun actionPerformed(event: AnActionEvent, notification: Notification) {
                    }
                }
            )
        )
    }
}
