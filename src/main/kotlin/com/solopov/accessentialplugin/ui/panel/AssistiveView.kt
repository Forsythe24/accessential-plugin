package com.solopov.accessentialplugin.ui.panel

import android.databinding.tool.ext.toCamelCase
import com.solopov.accessentialplugin.android_debug_bridge.AndroidDebugBridgeScript
import com.solopov.accessentialplugin.android_debug_bridge.params.AndroidDebugBridgeKeyCode
import com.solopov.accessentialplugin.android_debug_bridge.params.TBAction
import com.solopov.accessentialplugin.android_debug_bridge.params.TBGranularity
import com.solopov.accessentialplugin.android_debug_bridge.params.TBSetting
import com.solopov.accessentialplugin.android_debug_bridge.params.TBVolumeSetting
import com.solopov.accessentialplugin.android_debug_bridge.configureTalkBackService
import com.solopov.accessentialplugin.controller.Controller
import com.solopov.accessentialplugin.utils.getString
import com.solopov.accessentialplugin.ui.component.LargeButton
import com.solopov.accessentialplugin.utils.createDropDownMenu
import com.solopov.accessentialplugin.utils.createToggleRow
import com.solopov.accessentialplugin.utils.placeComponent
import com.solopov.accessentialplugin.utils.setMaxSize
import javax.swing.JLabel
import javax.swing.JPanel
import java.awt.GridBagConstraints
import java.awt.GridBagLayout

class AssistiveView(deviceController: Controller) : ControllerView(deviceController) {
    private val labelToggle = getString("panel.talkback.label.talkback")
    private val labelSpeech = getString("panel.talkback.label.talkback.speech")
    private val labelVolume = getString("panel.talkback.label.talkback.volume")
    private val labelNavigation = getString("panel.talkback.label.talkback.navigation")
    private val labelGranularity = getString("panel.talkback.label.granularity")
    private val labelBlock = getString("panel.talkback.blockout")

    private val toggleOn = getString("panel.talkback.button.talkback.on")
    private val toggleOff = getString("panel.talkback.button.talkback.off")
    private val speechOn = getString("panel.talkback.button.talkback.speech.on")
    private val speechOff = getString("panel.talkback.button.talkback.speech.off")
    private val volumeHalf = getString("panel.talkback.button.talkback.volume.medium")
    private val volumeMin = getString("panel.talkback.button.talkback.volume.low")
    private val navPrev = getString("panel.talkback.button.previous")
    private val navNext = getString("panel.talkback.button.next")
    private val navTap = getString("panel.talkback.button.tap")
    private val navLongTap = getString("panel.talkback.button.longTap")
    private val navHome = getString("panel.talkback.button.home")
    private val navBack = getString("panel.talkback.button.back")
    private val navMenu = getString("panel.talkback.button.menu")
    private val navActions = getString("panel.talkback.button.actions")
    private val blockEnable = getString("panel.talkback.blockout.on")
    private val blockDisable = getString("panel.talkback.blockout.off")

    private val granularityItems = listOf(
        "panel.talkback.label.granularity.default",
        "panel.talkback.label.granularity.headings",
        "panel.talkback.label.granularity.controls",
        "panel.talkback.label.granularity.links",
        "panel.talkback.label.granularity.words",
        "panel.talkback.label.granularity.paragraphs",
        "panel.talkback.label.granularity.characters",
        "panel.talkback.label.granularity.lines",
        "panel.talkback.label.granularity.window"
    )

    private var currentGranularity: TBGranularity = TBGranularity.Default

    override fun createPanelContent(): JPanel = JPanel().apply {
        layout = GridBagLayout()
        addToggleSection(0)
        addGranularityDropdown(1)
        addPrimaryButtons(2)
        addSystemActions(3)
        addVolumeSwitch(4)
        addSpeechControl(5)
        addBlockControl(6)
    }

    private fun JPanel.addToggleSection(row: Int) {
        createToggleRow(labelToggle, row, toggleOn, toggleOff,
            positiveAction = { configureTalkBackService(true).executeAdbCommand() },
            negativeAction = { configureTalkBackService(false).executeAdbCommand() })
    }

    private fun JPanel.addSpeechControl(row: Int) {
        createToggleRow(labelSpeech, row, speechOn, speechOff,
            positiveAction = {
                AndroidDebugBridgeScript.ModifyTalkBackSetting(TBSetting.TOGGLE_SPEECH_OUTPUT, true).executeAdbCommand()
            },
            negativeAction = {
                AndroidDebugBridgeScript.ModifyTalkBackSetting(TBSetting.TOGGLE_SPEECH_OUTPUT, false).executeAdbCommand()
            }
        )
    }

    private fun JPanel.addVolumeSwitch(row: Int) {
        createToggleRow(labelVolume, row, volumeHalf, volumeMin,
            positiveAction = {
                AndroidDebugBridgeScript.AdjustVolume(TBVolumeSetting.HALF).executeAdbCommand()
            },
            negativeAction = {
                AndroidDebugBridgeScript.AdjustVolume(TBVolumeSetting.MIN).executeAdbCommand()
            }
        )
    }

    private fun JPanel.addGranularityDropdown(row: Int) {
        createDropDownMenu(labelGranularity, row, granularityItems) {
            currentGranularity = TBGranularity.valueOf(it.removePrefix("panel.talkback.label.granularity.").toCamelCase())
        }
    }

    private fun JPanel.addBlockControl(row: Int) {
        createToggleRow(labelBlock, row, blockEnable, blockDisable,
            positiveAction = {
                AndroidDebugBridgeScript.ModifyTalkBackSetting(TBSetting.BLOCK_OUT, true).executeAdbCommand()
            },
            negativeAction = {
                AndroidDebugBridgeScript.ModifyTalkBackSetting(TBSetting.BLOCK_OUT, false).executeAdbCommand()
            }
        )
    }

    private fun JPanel.addPrimaryButtons(row: Int) {
        placeComponent(JLabel(labelNavigation).apply { setMaxSize() }, x = 0, y = row, w = 2)

        val navButtons = listOf(
            Pair(navPrev, TBAction.PREVIOUS),
            Pair(navNext, TBAction.NEXT),
            Pair(navTap, TBAction.PERFORM_CLICK_ACTION),
            Pair(navLongTap, TBAction.PERFORM_LONG_CLICK_ACTION)
        )

        navButtons.forEachIndexed { index, (label, action) ->
            placeComponent(
                LargeButton(label, "") {
                    AndroidDebugBridgeScript.TalkBackNavigation(action, currentGranularity).executeAdbCommand()
                }.create(),
                x = index + 3, y = row, fillType = GridBagConstraints.BOTH
            )
        }
    }

    private fun JPanel.addSystemActions(row: Int) {
        val systemButtons = listOf(
            Pair(navBack, AndroidDebugBridgeScript.SendKeyEvent(AndroidDebugBridgeKeyCode.BACK)),
            Pair(navHome, AndroidDebugBridgeScript.SendKeyEvent(AndroidDebugBridgeKeyCode.HOME)),
            Pair(navMenu, AndroidDebugBridgeScript.TalkBackNavigation(TBAction.TALKBACK_BREAKOUT)),
            Pair(navActions, AndroidDebugBridgeScript.TalkBackNavigation(TBAction.SHOW_CUSTOM_ACTIONS))
        )

        systemButtons.forEachIndexed { index, (label, script) ->
            placeComponent(
                LargeButton(label, "") { script.executeAdbCommand() }.create(),
                x = index + 3, y = row, fillType = GridBagConstraints.BOTH
            )
        }
    }
}