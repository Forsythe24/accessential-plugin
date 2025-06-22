package com.solopov.accessentialplugin.ui.panel

import com.solopov.accessentialplugin.controller.Controller
import com.solopov.accessentialplugin.utils.getString
import com.solopov.accessentialplugin.model.Device
import com.solopov.accessentialplugin.utils.addFiller
import com.solopov.accessentialplugin.utils.setMaxSize
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class AvailableDevicesView(private val controller: Controller) {
    private val deviceNotifier = controller.connectedDevicesNotifier
    private val devicesView: JPanel by lazy {
        JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS) }
    }

    @Suppress("LongMethod")
    private fun buildDeviceCheckboxPanel(device: Device) = JPanel().apply {
        layout = BorderLayout()
        setMaxSize()

        val deviceNameLabel = JLabel(device.serial)
        val detailsLabel = JLabel("...").apply {
            font = font.deriveFont(Font.ITALIC, font.size - 3f)
        }

        add(JPanel().apply {
            layout = FlowLayout(FlowLayout.LEFT)

            val checkbox = JCheckBox().apply {
                isSelected = device.serial in controller.selectedDeviceSerialList
                addChangeListener {
                    toggleDeviceSelection(device.serial, isSelected)
                }
            }

            fun handleSelectionToggle() {
                checkbox.isSelected = !checkbox.isSelected
            }

            addKeyListener(object : KeyAdapter() {
                override fun keyReleased(e: KeyEvent) {
                    if (e.keyCode == KeyEvent.VK_SPACE) handleSelectionToggle()
                }
            })

            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) = handleSelectionToggle()
            })

            add(checkbox)
            add(JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                add(deviceNameLabel)
                add(detailsLabel)
            })
        }, BorderLayout.WEST)

        name = device.serial
        device.fetchDeviceInfo()
            .firstElement()
            .subscribe { info ->
                name = info.name
                deviceNameLabel.text = info.name
                detailsLabel.text = "${device.serial} - [${info.api} / ${info.sdkLevel}]"
                addTb4dInstallButtonIfNeeded(this, device, info.packageList)
            }
    }

    private fun toggleDeviceSelection(serial: String, selected: Boolean) {
        if (selected) {
            controller.selectedDeviceSerialList.add(serial)
        } else {
            controller.selectedDeviceSerialList.remove(serial)
        }
    }

    private fun addTb4dInstallButtonIfNeeded(container: JPanel, device: Device, packages: List<String>) {
        val buttonPanel = JPanel().apply { layout = FlowLayout(FlowLayout.RIGHT) }

        val installButton = JButton(getString("panel.device.label.install.tb4d")).apply {
            addActionListener {
                isEnabled = false
                device.installTalkBackForDevelopers()
                    .firstElement()
                    .subscribe { success ->
                        isEnabled = true
                        if (success) {
                            controller.showInstallTB4DSuccessNotification(device)
                            container.remove(buttonPanel)
                        } else {
                            controller.showInstallTB4DErrorNotification(device)
                        }
                    }
            }
        }

        val webButton = JButton().apply {
            addActionListener { }
        }

        buttonPanel.add(installButton)
        buttonPanel.add(webButton)
        container.add(buttonPanel, BorderLayout.EAST)
    }

    fun create() = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        add(JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            setMaxSize()

            add(JLabel(getString("panel.device.title")).apply {
                setMaxSize()
                font = font.deriveFont(Font.BOLD, font.size + 2f)
            })

            add(Box.createHorizontalGlue())

            add(JButton(getString("panel.device.refresh")).apply {
                setMaxSize()
                addActionListener {
                    devicesView.apply {
                        removeAll()
                        add(JLabel(getString("panel.device.wait")).apply {
                            font = font.deriveFont(Font.BOLD, font.size + 5f)
                        }, BorderLayout.CENTER)
                    }
                    controller.updateAndroidDebugBridge()
                }
            })
        })

        add(devicesView)
        deviceNotifier
            .distinctUntilChanged()
            .subscribe { devices ->
                devicesView.removeAll()
                if (devices.isEmpty()) {
                    devicesView.add(JLabel(getString("panel.device.no_devices")))
                } else {
                    devices.sortedWith(compareBy(
                        { it.isEmulator },
                        { it.displayName }
                    )).forEach {
                        devicesView.add(buildDeviceCheckboxPanel(it))
                    }
                    devicesView.addFiller()
                }
            }
    }
}