package com.solopov.accessentialplugin.controller

import com.android.ddmlib.AndroidDebugBridge
import java.util.concurrent.TimeUnit

class AndroidDebugBridgeProviderImpl:
    AndroidDebugBridgeProvider {
    @Suppress("MagicNumber")
    private val restartTimeout = 500L
    override fun setChangeListener(listener: AndroidDebugBridge.IDeviceChangeListener) =
        AndroidDebugBridge.addDeviceChangeListener(listener)

    @Suppress("SwallowedException")
    override fun updateAndroidDebugBridge() {
        try {
            AndroidDebugBridge.getBridge().restart(restartTimeout, TimeUnit.MILLISECONDS)
        } catch (e: IllegalStateException) {
            println("Prevented crash from restart")
        }
    }
}