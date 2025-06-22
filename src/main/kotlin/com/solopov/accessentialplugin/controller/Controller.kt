package com.solopov.accessentialplugin.controller

import com.solopov.accessentialplugin.model.Device
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import io.reactivex.rxjava3.core.Observable

interface Controller {
    val connectedDevicesNotifier: Observable<Set<Device>>
    var selectedDeviceSerialList: MutableSet<String>

    fun showInstallTB4DSuccessNotification(device: Device)
    fun showInstallTB4DErrorNotification(device: Device)
    fun runOnAllValidSelectedDevices(fn: (Device) -> Unit)
    fun updateAndroidDebugBridge()

    fun showNotification(
        title: String,
        message: String,
        type: NotificationType,
        actions: Collection<NotificationAction> = emptyList()
    )
}