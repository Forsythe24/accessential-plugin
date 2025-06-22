package com.solopov.accessentialplugin.controller

import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener
import com.android.ddmlib.IDevice
import com.solopov.accessentialplugin.utils.getString
import com.solopov.accessentialplugin.model.Device
import com.solopov.accessentialplugin.model.AndroidStudioPluginNotificationInfo
import com.solopov.accessentialplugin.model.NotificationInfo
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class AndroidStudioPluginController(
    private val project: Project,
    private val androidDebugBridgeProviderImpl: AndroidDebugBridgeProviderImpl = AndroidDebugBridgeProviderImpl()
) : Controller {
    private val groupId = "Accessential"
    private val connectedDeviceList: MutableList<Device> = mutableListOf()
    private val deviceSource = BehaviorSubject.create<Set<Device>>()
    override var selectedDeviceSerialList: MutableSet<String> = mutableSetOf()

    override val connectedDevicesNotifier: Observable<Set<Device>> = deviceSource

    init {
        androidDebugBridgeProviderImpl.setChangeListener(object : IDeviceChangeListener {
            private fun updateDeviceList(device: IDevice, updateDevice: Boolean = true) {
                connectedDeviceList.removeIf { it.serial == device.serialNumber }
                if (updateDevice) {
                    connectedDeviceList.add(Device(device))
                }
                deviceSource.onNext(connectedDeviceList.toSet())
            }

            override fun deviceConnected(device: IDevice?) {
                if (device != null) {
                    updateDeviceList(device)
                }
            }

            override fun deviceDisconnected(device: IDevice?) {
                if (device != null) {
                    updateDeviceList(device, false)
                }
            }

            // Do not update the device list - it creates a backlog of requests on the device
            override fun deviceChanged(device: IDevice?, changeMask: Int) {
                if (device != null) {
                    val sb = StringBuilder()
                    if (changeMask and IDevice.CHANGE_BUILD_INFO != 0) {
                        sb.append("CHANGE_BUILD_INFO")
                    }
                    if (changeMask and IDevice.CHANGE_STATE != 0) {
                        if (sb.isNotBlank()) sb.append(" ")
                        sb.append("CHANGE_STATE")
                    }
                    if (changeMask and IDevice.CHANGE_CLIENT_LIST != 0) {
                        if (sb.isNotBlank()) sb.append(" ")
                        sb.append("CHANGE_CLIENT_LIST")
                    }

                    if (changeMask and IDevice.CHANGE_PROFILEABLE_CLIENT_LIST != 0) {
                        if (sb.isNotBlank()) sb.append(" ")
                        sb.append("CHANGE_PROFILEABLE_CLIENT_LIST")
                    }
                }
            }
        })
    }

    override fun showNotification(
        title: String,
        message: String,
        type: NotificationType,
        actions: Collection<NotificationAction>
    ) {
        showNotification(
            AndroidStudioPluginNotificationInfo(
                title = title,
                message = message,
                notificationType = type,
                actionsList = actions
            )
        )
    }

    override fun showInstallTB4DSuccessNotification(device: Device) {
        showNotification(AndroidStudioPluginNotificationInfo(
            getString("tb4d.install.success.title"),
            getString("tb4d.install.success.message", device.displayName),
            actionsList = listOf(
                object : NotificationAction(getString("tb4d.install.success.action")) {
                    override fun actionPerformed(event: AnActionEvent, notification: Notification) {}
                }
            )
        ))
    }

    override fun showInstallTB4DErrorNotification(device: Device) {
        showNotification(AndroidStudioPluginNotificationInfo(
            getString("tb4d.install.error.title"),
            getString("tb4d.install.error.message"),
            actionsList = listOf(
                object : NotificationAction(getString("tb4d.install.error.action")) {
                    override fun actionPerformed(event: AnActionEvent, notification: Notification) {}
                }
            )
        ))
    }

    private fun showNoSelectedDevicesNotification() {
        showNotification(AndroidStudioPluginNotificationInfo(
            getString("plugin.notification.no_devices.title"),
            getString("plugin.notification.no_devices.message"),
            notificationType = NotificationType.ERROR,
            actionsList = emptyList()
        ))
    }

    override fun runOnAllValidSelectedDevices(fn: (Device) -> Unit) {
        if (connectedDeviceList.size == 1) {
            fn(connectedDeviceList[0])
        } else {
            connectedDeviceList
                .filter { it.serial in selectedDeviceSerialList }
                .map { fn(it) }
                .ifEmpty { showNoSelectedDevicesNotification() }
        }
    }

    override fun updateAndroidDebugBridge() = androidDebugBridgeProviderImpl.updateAndroidDebugBridge()

    private fun <NPT : NotificationInfo> showNotification(notificationPayload: NPT) {
        require(notificationPayload is AndroidStudioPluginNotificationInfo)

        NotificationGroupManager
            .getInstance()
            .getNotificationGroup(groupId)
            .createNotification(
                notificationPayload.title,
                notificationPayload.message,
                notificationPayload.notificationType
            )
            .addActions(notificationPayload.actionsList)
            .notify(project)
    }
}