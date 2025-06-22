package com.solopov.accessentialplugin.android_debug_bridge

import com.solopov.accessentialplugin.android_debug_bridge.params.AndroidDebugBridgeKeyCode
import com.solopov.accessentialplugin.android_debug_bridge.params.AdbParameter
import com.solopov.accessentialplugin.android_debug_bridge.params.SystemConfigurationScope
import com.solopov.accessentialplugin.android_debug_bridge.params.TBAction
import com.solopov.accessentialplugin.android_debug_bridge.params.TBGranularity
import com.solopov.accessentialplugin.android_debug_bridge.params.TBSetting
import com.solopov.accessentialplugin.android_debug_bridge.params.TBVolumeSetting
import java.io.BufferedReader
import java.io.File

@Suppress("TooManyFunctions")
sealed class AndroidDebugBridgeScript {
    data class ServiceConfiguration(
        val enable: Boolean,
        val needsTouchExploration: Boolean,
        val packageIdentifier: String,
        val serviceComponent: String
    ) : AndroidDebugBridgeScript()

    data class ScriptFile(
        val filePath: String,
        val arguments: List<String> = emptyList()
    ) : AndroidDebugBridgeScript()

    data class TalkBackNavigation(
        val navigationCommand: TBAction,
        val movementPrecision: TBGranularity = TBGranularity.Default
    ) : AndroidDebugBridgeScript()

    data class AdjustVolume(
        val volumeLevel: TBVolumeSetting
    ) : AndroidDebugBridgeScript()

    data class ModifyTalkBackSetting(
        val setting: TBSetting,
        val isEnabled: Boolean
    ) : AndroidDebugBridgeScript()

    data class SendKeyEvent(val keyCode: AndroidDebugBridgeKeyCode) : AndroidDebugBridgeScript()
    data class SystemConfiguration<T : AdbParameter>(
        val configName: String,
        val scope: SystemConfigurationScope,
        val value: T
    ) : AndroidDebugBridgeScript()

    data class ShellCommand<T : AdbParameter>(
        val command: String,
        val params: List<T> = listOf()
    ) : AndroidDebugBridgeScript()

    data class CommandSequence(
        val commands: List<AndroidDebugBridgeScript>
    ) : AndroidDebugBridgeScript()

    fun toShellCommand(): String =
        when (this) {
            is ModifyTalkBackSetting -> createSettingModificationCommand()
            is SendKeyEvent -> createKeyEventCommand()
            is SystemConfiguration<*> -> createSystemConfigCommand()

            is CommandSequence -> combineCommands()
            is ServiceConfiguration -> buildServiceCommand()
            is ShellCommand<*> -> buildShellCommand()
            is TalkBackNavigation -> createTalkBackNavigationCommand()

            is AdjustVolume -> createVolumeAdjustmentCommand()
            is ScriptFile -> executeScriptFile()
        }

    private fun buildServiceCommand(): String {
        require(this is ServiceConfiguration)
        val fullServicePath = "$packageIdentifier/$serviceComponent"
        val touchConfig = if (needsTouchExploration) {
            val state = if (enable) "1" else "0"
            " settings put secure accessibility_enabled $state; settings put secure touch_exploration_enabled $state;"
        } else {
            ""
        }
        return if (enable) {
            "CURRENT=\$(settings get secure enabled_accessibility_services); if [[ \"\$CURRENT\" != *\"$fullServicePath\"* ]]; then if [[ -z \"\$CURRENT\" || \"\$CURRENT\" == \"null\" ]]; then settings put secure enabled_accessibility_services $fullServicePath; else settings put secure enabled_accessibility_services \$CURRENT:$fullServicePath; fi;$touchConfig fi"
        } else {
            "CURRENT=\$(settings get secure enabled_accessibility_services); if [[ \"\$CURRENT\" == *\"$fullServicePath\"* ]]; then SERVICE=$fullServicePath; FINAL_RESULT=\"\${CURRENT//:\$SERVICE/}\"; FINAL_RESULT=\"\${FINAL_RESULT//SERVICE:/}\"; FINAL_RESULT=\"\${FINAL_RESULT//\$SERVICE/}\"; settings put secure enabled_accessibility_services \"\$FINAL_RESULT\";$touchConfig fi"
        }
    }

    private fun executeScriptFile(): String {
        require(this is ScriptFile)
        val bufferedReader: BufferedReader = File(filePath).bufferedReader()
        var scriptContent = bufferedReader.use { it.readText() }.trim()
        arguments.forEachIndexed { index, arg ->
            scriptContent = scriptContent.replace("$${index + 1}", arg)
        }
        return scriptContent
    }

    private fun createTalkBackNavigationCommand(): String {
        require(this is TalkBackNavigation)
        return StringBuilder().apply {
            append(broadcastCommand)
            append(" ")
            append(talkBackPackage)
            append(".")
            append(navigationCommand.name.lowercase())

            if (navigationCommand in listOf(TBAction.NEXT, TBAction.PREVIOUS)) {
                append(movementPrecision.getGranularitySuffix())
            }
        }.toString()
    }

    private fun createVolumeAdjustmentCommand(): String {
        require(this is AdjustVolume)
        return StringBuilder().apply {
            append(broadcastCommand)
            append(" ")
            append(talkBackPackage)
            append(".")
            append(volumeLevel.name.lowercase())
        }.toString()
    }

    private fun createSettingModificationCommand(): String {
        require(this is ModifyTalkBackSetting)

        return StringBuilder().apply {
            append(broadcastCommand)
            append(" ")
            append(talkBackPackage)
            append(".")
            append(setting.name.lowercase())
            append(" -e value $isEnabled")
        }.toString()
    }

    private fun createKeyEventCommand(): String {
        require(this is SendKeyEvent)
        return "input keyevent ${keyCode.androidValue}"
    }

    private fun createSystemConfigCommand(): String {
        require(this is SystemConfiguration<*>)
        return "settings put ${scope.name.lowercase()} $configName ${value.toAdbArgument()}"
    }

    private fun buildShellCommand(): String {
        require(this is ShellCommand<*>)
        var finalCommand = command
        params.forEachIndexed { index, param ->
            finalCommand = finalCommand.replace("$${index + 1}", param.toAdbArgument())
        }
        return finalCommand
    }

    private fun combineCommands(): String {
        require(this is CommandSequence)
        var separator = ""
        return StringBuilder().apply {
            commands.forEach { cmd ->
                append(separator)
                append(cmd.toShellCommand())
                separator = "; "
            }
        }.toString()
    }

    private fun TBGranularity.getGranularitySuffix()
            : String {
        return StringBuilder("").apply {
            append(" ")
            append(broadcastParameter)
            append(" ")
            append(this@getGranularitySuffix.talkBackValue)
        }.toString()
    }

    private val broadcastCommand = "am broadcast -a"
    private val broadcastParameter = "-e mode"
    private val talkBackPackage = "com.solopov.adb"
}