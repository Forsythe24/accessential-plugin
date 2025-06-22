package com.solopov.accessentialplugin.ui.panel

import com.android.tools.lint.detector.api.isNumberString
import com.solopov.accessentialplugin.android_debug_bridge.toggleAnimations
import com.solopov.accessentialplugin.android_debug_bridge.configureColorAdjustment
import com.solopov.accessentialplugin.android_debug_bridge.toggleColorInversion
import com.solopov.accessentialplugin.android_debug_bridge.toggleDarkMode
import com.solopov.accessentialplugin.android_debug_bridge.adjustDisplayDensity
import com.solopov.accessentialplugin.android_debug_bridge.params.ColorAdjustmentMode
import com.solopov.accessentialplugin.controller.Controller
import com.solopov.accessentialplugin.utils.getString
import com.solopov.accessentialplugin.utils.createToggleRow
import com.solopov.accessentialplugin.utils.placeComponent
import com.solopov.accessentialplugin.utils.setMaxSize
import com.intellij.openapi.ui.ComboBox
import com.solopov.accessentialplugin.ui.component.Button
import javax.swing.DefaultComboBoxModel
import javax.swing.JLabel
import javax.swing.JPanel
import java.awt.GridBagLayout

class DisplaySettingsView(deviceController: Controller) : ControllerView(deviceController) {
    private val labelDarkMode = getString("panel.display.dark.label")
    private val btnDarkOn = getString("panel.display.dark.on")
    private val btnDarkOff = getString("panel.display.dark.off")

    private val labelAnimations = getString("panel.display.animation.label")
    private val btnAnimOn = getString("panel.display.animation.on")
    private val btnAnimOff = getString("panel.display.animation.off")

    private val labelInversion = getString("panel.display.inversion.label")
    private val btnInvertOn = getString("panel.display.inversion.on")
    private val btnInvertOff = getString("panel.display.inversion.off")

    private val labelDensity = getString("panel.display.density.label")
    private val defaultDensity = getString("panel.display.density.default")

    private val labelColorMode = getString("panel.display.label.correction")
    private val colorModes = listOf(
        "panel.display.label.correction.off",
        "panel.display.label.correction.greyscale",
        "panel.display.label.correction.deuteranomaly",
        "panel.display.label.correction.tritanomaly",
        "panel.display.label.correction.protanomaly"
    )

    override fun createPanelContent(): JPanel = JPanel(GridBagLayout()).apply {
        addDensityControls(0) { selected ->
            val densityValue = if (isNumberString(selected)) selected.toInt() else -1
            adjustDisplayDensity(densityValue).executeAdbCommand()
        }

        addDarkModeToggle(1)
        addAnimationsToggle(2)
        addInversionToggle(3)

        addColorModeSelector(4) { option ->
            val optionName = option.split(".").last().uppercase()
            configureColorAdjustment(ColorAdjustmentMode.valueOf(optionName)).executeAdbCommand()
        }
    }

    private fun JPanel.addDensityControls(row: Int, onValue: (String) -> Unit) {
        placeComponent(JLabel(labelDensity).apply { setMaxSize() }, x = 0, y = row, w = 1)

        val densityOptions = listOf("356", defaultDensity, "460", "540", "500")
        densityOptions.forEachIndexed { i, label ->
            placeComponent(Button(buttonText = label, onClick = {
                onValue(if (isNumberString(label)) label else "")
            }).create(), x = 2 + i, y = row, w = 1)
        }
    }

    private fun JPanel.addDarkModeToggle(row: Int) {
        createToggleRow(
            label = labelDarkMode,
            whichRow = row,
            positiveLabel = btnDarkOn,
            negativeLabel = btnDarkOff,
            positiveAction = { toggleDarkMode(true).executeAdbCommand() },
            negativeAction = { toggleDarkMode(false).executeAdbCommand() }
        )
    }

    private fun JPanel.addAnimationsToggle(row: Int) {
        createToggleRow(
            label = labelAnimations,
            whichRow = row,
            positiveLabel = btnAnimOn,
            negativeLabel = btnAnimOff,
            positiveAction = { toggleAnimations(true).executeAdbCommand() },
            negativeAction = { toggleAnimations(false).executeAdbCommand() }
        )
    }

    private fun JPanel.addInversionToggle(row: Int) {
        createToggleRow(
            label = labelInversion,
            whichRow = row,
            positiveLabel = btnInvertOn,
            negativeLabel = btnInvertOff,
            positiveAction = { toggleColorInversion(true).executeAdbCommand() },
            negativeAction = { toggleColorInversion(false).executeAdbCommand() }
        )
    }

    private fun JPanel.addColorModeSelector(row: Int, onSelect: (String) -> Unit) {
        placeComponent(JLabel(labelColorMode).apply { setMaxSize() }, x = 0, y = row, w = 2)

        val comboBox = ComboBox(DefaultComboBoxModel<String>().apply {
            colorModes.forEach { addElement(getString(it)) }
        }).apply {
            setMaxSize()
            addActionListener {
                onSelect(colorModes[selectedIndex])
            }
        }

        placeComponent(comboBox, x = 3, y = row, w = 4)
    }
}