package com.solopov.accessentialplugin.android_debug_bridge.params

@Suppress("MagicNumber")
enum class ColorAdjustmentMode(val adbParameterValue: Int) {
    OFF(-1),
    GREYSCALE(0),
    PROTANOMALY(11),
    DEUTERANOMALY(12),
    TRITANOMALY(13),
}