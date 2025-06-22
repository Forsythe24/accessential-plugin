package com.solopov.accessentialplugin.controller

import com.android.ddmlib.AndroidDebugBridge

interface AndroidDebugBridgeProvider {
    fun setChangeListener(listener: AndroidDebugBridge.IDeviceChangeListener)

    fun updateAndroidDebugBridge()
}