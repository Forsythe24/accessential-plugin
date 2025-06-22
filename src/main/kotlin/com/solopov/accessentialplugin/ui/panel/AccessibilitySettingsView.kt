package com.solopov.accessentialplugin.ui.panel

import com.solopov.accessentialplugin.android_debug_bridge.toggleAudioDescription
import com.solopov.accessentialplugin.android_debug_bridge.toggleBoldText
import com.solopov.accessentialplugin.android_debug_bridge.toggleCaptions
import com.solopov.accessentialplugin.android_debug_bridge.setFontScaling
import com.solopov.accessentialplugin.android_debug_bridge.toggleHighContrastText
import com.solopov.accessentialplugin.android_debug_bridge.setInteractionTimeout
import com.solopov.accessentialplugin.controller.Controller
import com.solopov.accessentialplugin.ui.component.Button
import com.solopov.accessentialplugin.utils.getString
import com.solopov.accessentialplugin.utils.createDropDownMenu
import com.solopov.accessentialplugin.utils.createToggleRow
import com.solopov.accessentialplugin.utils.placeComponent
import com.solopov.accessentialplugin.utils.setMaxSize
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSlider
import java.awt.GridBagConstraints
import java.awt.GridBagLayout

class AccessibilitySettingsView(deviceController: Controller) : ControllerView(deviceController) {
    private val labelCaptions = getString("panel.settings.label.captions")
    private val btnCaptionsEnable = getString("panel.settings.button.captions.on")
    private val btnCaptionsDisable = getString("panel.settings.button.captions.off")

    private val labelAudioDesc = getString("panel.settings.description.label")
    private val btnAudioOn = getString("panel.settings.description.on")
    private val btnAudioOff = getString("panel.settings.description.off")

    private val labelTimeout = getString("panel.settings.label.reaction")
    private val timeoutChoices = listOf(
        "panel.settings.label.reaction.default",
        "panel.settings.label.reaction.ten",
        "panel.settings.label.reaction.thirty",
        "panel.settings.label.reaction.minute",
        "panel.settings.label.reaction.minutes"
    )

    private val labelFontScale = getString("panel.font.label.scale")
    private val btnResetFont = getString("panel.font.label.reset")

    private val labelBoldText = getString("panel.font.bold.label")
    private val btnBoldEnable = getString("panel.font.bold.on")
    private val btnBoldDisable = getString("panel.font.bold.off")

    private val labelHighContrast = getString("panel.font.contrast.label")
    private val btnContrastEnable = getString("panel.font.contrast.on")
    private val btnContrastDisable = getString("panel.font.contrast.off")

    private val defaultScale = 100

    override fun createPanelContent(): JPanel = JPanel(GridBagLayout()).apply {
        addFontScaleSlider(0) { scaleFactor ->
            setFontScaling(scaleFactor).executeAdbCommand()
        }

        addBoldTextSwitch(1)
        addContrastSwitch(2)

        addInteractionTimeoutDropdown(3) { selected ->
            val millis = when (selected) {
                "panel.settings.label.reaction.ten" -> 10_000
                "panel.settings.label.reaction.thirty" -> 30_000
                "panel.settings.label.reaction.minute" -> 60_000
                "panel.settings.label.reaction.minutes" -> 120_000
                else -> 0
            }
            setInteractionTimeout(millis).executeAdbCommand()
        }

        addCaptionsToggle(4)
        addAudioDescriptionToggle(5)
    }

    private fun JPanel.addInteractionTimeoutDropdown(row: Int, onSelect: (String) -> Unit) {
        createDropDownMenu(labelTimeout, row, timeoutChoices, onSelect)
    }

    private fun JPanel.addCaptionsToggle(row: Int) {
        createToggleRow(
            labelCaptions,
            row,
            btnCaptionsEnable,
            btnCaptionsDisable,
            positiveAction = { toggleCaptions(true).executeAdbCommand() },
            negativeAction = { toggleCaptions(false).executeAdbCommand() }
        )
    }

    private fun JPanel.addAudioDescriptionToggle(row: Int) {
        createToggleRow(
            labelAudioDesc,
            row,
            btnAudioOn,
            btnAudioOff,
            positiveAction = { toggleAudioDescription(true).executeAdbCommand() },
            negativeAction = { toggleAudioDescription(false).executeAdbCommand() }
        )
    }

    private fun JPanel.addFontScaleSlider(row: Int, onScaleChanged: (Float) -> Unit) {
        val scaleLabel = JLabel(labelFontScale).apply { setMaxSize() }

        val slider = JSlider(50, 300, defaultScale).apply {
            setMaxSize()
            paintTicks = true
            paintLabels = true
            paintTrack = true
            majorTickSpacing = 50
            minorTickSpacing = 10
            snapToTicks = true

            addChangeListener {
                if (!valueIsAdjusting) {
                    onScaleChanged(value.toFloat() / defaultScale)
                }
            }
        }

        val resetBtn = Button(buttonText = btnResetFont, onClick = {slider.value = defaultScale}).create().apply {
        }

        placeComponent(scaleLabel, x = 0, y = row, w = 1, anchorType = GridBagConstraints.CENTER)
        placeComponent(resetBtn, x = 1, y = row, w = 1, anchorType = GridBagConstraints.CENTER)
        placeComponent(slider, x = 3, y = row, w = 4)
    }

    private fun JPanel.addBoldTextSwitch(row: Int) {
        createToggleRow(
            labelBoldText,
            row,
            btnBoldEnable,
            btnBoldDisable,
            positiveAction = { toggleBoldText(true).executeAdbCommand() },
            negativeAction = { toggleBoldText(false).executeAdbCommand() }
        )
    }

    private fun JPanel.addContrastSwitch(row: Int) {
        createToggleRow(
            labelHighContrast,
            row,
            btnContrastEnable,
            btnContrastDisable,
            positiveAction = { toggleHighContrastText(true).executeAdbCommand() },
            negativeAction = { toggleHighContrastText(false).executeAdbCommand() }
        )
    }
}