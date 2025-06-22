@file:Suppress("TooManyFunctions")
package com.solopov.accessentialplugin.android_debug_bridge

import com.solopov.accessentialplugin.android_debug_bridge.params.*

fun setFontScaling(factor: Float) = AndroidDebugBridgeScript.SystemConfiguration(
    configName = "font_scale",
    scope = SystemConfigurationScope.SYSTEM_SETTINGS,
    value = GenericAdbParameter(factor)
)

fun toggleBoldText(enabled: Boolean) = AndroidDebugBridgeScript.SystemConfiguration(
    configName = "font_weight_adjustment",
    scope = SystemConfigurationScope.SECURE_SETTINGS,
    value = GenericAdbParameter(if (enabled) "300" else "0")
)

fun setInteractionTimeout(value: Int) = AndroidDebugBridgeScript.SystemConfiguration(
    configName = "accessibility_interactive_ui_timeout_ms",
    scope = SystemConfigurationScope.SECURE_SETTINGS,
    value = GenericAdbParameter(value)
)

fun configureAccessibilityScanner(enable: Boolean) = AndroidDebugBridgeScript.ServiceConfiguration(
    enable,
    false,
    "com.google.android.apps.accessibility.auditor",
    "com.google.android.apps.accessibility.auditor.ScannerService"
)

fun configureTalkBackService(enable: Boolean) = AndroidDebugBridgeScript.ServiceConfiguration(
    enable,
    true,
    "com.android.talkback4d",
    "com.developer.talkback.TalkBackDevService"
)

fun configureVoiceAccess(enable: Boolean) = AndroidDebugBridgeScript.ServiceConfiguration(
    enable,
    false,
    "com.google.android.apps.accessibility.voiceaccess",
    "com.google.android.apps.accessibility.voiceaccess.JustSpeakService"
)

fun configureSwitchAccess(enable: Boolean) = AndroidDebugBridgeScript.ServiceConfiguration(
    enable,
    false,
    "com.google.android.accessibility.switchaccess",
    "com.android.switchaccess.SwitchAccessService"
)

fun adjustDisplayDensity(value: Int = -1) = AndroidDebugBridgeScript.ShellCommand(
    command = "wm density $1",
    params = listOf(NonNegativeAdbParameter(value, "reset"))
)

fun toggleDarkMode(enable: Boolean) = AndroidDebugBridgeScript.ShellCommand(
    command = "cmd uimode night $1",
    params = listOf(BooleanAdbParameter(enable, BooleanOutputFormat.YES_NO))
)

fun toggleColorInversion(enable: Boolean) = AndroidDebugBridgeScript.SystemConfiguration(
    configName = "accessibility_display_inversion_enabled",
    scope = SystemConfigurationScope.SECURE_SETTINGS,
    value = BooleanAdbParameter(enable, BooleanOutputFormat.NUMERIC)
)

fun toggleTouchIndication(enable: Boolean) = AndroidDebugBridgeScript.SystemConfiguration(
    configName = "show_touches",
    scope = SystemConfigurationScope.SYSTEM_SETTINGS,
    value = BooleanAdbParameter(enable, BooleanOutputFormat.NUMERIC)
)

fun toggleHighContrastText(enable: Boolean) = AndroidDebugBridgeScript.SystemConfiguration(
    configName = "high_text_contrast_enabled",
    scope = SystemConfigurationScope.SECURE_SETTINGS,
    value = BooleanAdbParameter(enable, BooleanOutputFormat.NUMERIC)
)

private fun enableColorAdjustment(enable: Boolean) = AndroidDebugBridgeScript.SystemConfiguration(
    configName = "accessibility_display_daltonizer_enabled",
    scope = SystemConfigurationScope.SECURE_SETTINGS,
    value = BooleanAdbParameter(enable, BooleanOutputFormat.NUMERIC)
)

private fun setColorAdjustmentMode(mode: ColorAdjustmentMode) = AndroidDebugBridgeScript.SystemConfiguration(
    configName = "accessibility_display_daltonizer",
    scope = SystemConfigurationScope.SECURE_SETTINGS,
    value = ColorAdjustmentAdbParameter(mode)
)

fun configureColorAdjustment(mode: ColorAdjustmentMode) = AndroidDebugBridgeScript.CommandSequence(
    if (mode == ColorAdjustmentMode.OFF) {
        listOf(enableColorAdjustment(false))
    } else {
        listOf(
            enableColorAdjustment(true),
            setColorAdjustmentMode(mode)
        )
    }
)

fun toggleLayoutBounds(enabled: Boolean) = AndroidDebugBridgeScript.ShellCommand(
    command = "setprop debug.layout $1; service call activity 1599295570 > /dev/null 2>&1",
    params = listOf(BooleanAdbParameter(enabled, BooleanOutputFormat.NUMERIC))
)

fun toggleAnimations(enabled: Boolean) = AndroidDebugBridgeScript.ShellCommand(
    command = "cmd settings put global animator_duration_scale $1; cmd settings put global transition_animation_scale $1; cmd settings put global window_animation_scale $1",
    params = listOf(BooleanAdbParameter(enabled, BooleanOutputFormat.DECIMAL))
)

fun toggleCaptions(enabled: Boolean) = AndroidDebugBridgeScript.SystemConfiguration(
    configName = "accessibility_captioning_enabled",
    scope = SystemConfigurationScope.SECURE_SETTINGS,
    value = BooleanAdbParameter(enabled, BooleanOutputFormat.NUMERIC)
)

fun toggleAudioDescription(enabled: Boolean) = AndroidDebugBridgeScript.CommandSequence(
    commands = listOf(
        AndroidDebugBridgeScript.SystemConfiguration(
            configName = "enabled_accessibility_audio_description_by_default",
            scope = SystemConfigurationScope.SECURE_SETTINGS,
            value = BooleanAdbParameter(enabled, BooleanOutputFormat.NUMERIC)
        ),
        AndroidDebugBridgeScript.SystemConfiguration(
            configName = "accessibility_audio_descriptions_enabled",
            scope = SystemConfigurationScope.SECURE_SETTINGS,
            value = BooleanAdbParameter(enabled, BooleanOutputFormat.NUMERIC)
        )
    )
)


fun openSettingsScreen(screen: SettingsScreen) =
    AndroidDebugBridgeScript.ShellCommand(
        "am start -a $1",
        listOf(GenericAdbParameter(screen.internalAndroidReference))
    )
