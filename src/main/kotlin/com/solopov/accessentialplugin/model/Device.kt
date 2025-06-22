package com.solopov.accessentialplugin.model

import android.databinding.tool.ext.capitalizeUS
import com.android.ddmlib.*
import com.solopov.accessentialplugin.APK_PHONE
import com.solopov.accessentialplugin.APK_WEAR
import com.solopov.accessentialplugin.utils.getString
import com.solopov.accessentialplugin.utils.onException
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.*
import java.io.File
import java.io.FileNotFoundException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class Device(private val underlyingDevice: IDevice) {

    val isEmulator: Boolean by lazy { underlyingDevice.isEmulator }
    val serial: String by lazy { underlyingDevice.serialNumber }
    val isWearable: Boolean by lazy {
        underlyingDevice.getProperty(IDevice.PROP_BUILD_CHARACTERISTICS)?.contains("watch") ?: false
    }

    private val androidVersion: String get() = underlyingDevice.getProperty(IDevice.PROP_BUILD_API_LEVEL) ?: ""
    private val sdkVersion: String get() = underlyingDevice.getProperty(IDevice.PROP_BUILD_VERSION) ?: ""

    val displayName: String
        get() = try {
            when {
                isEmulator -> getEmulatorName()
                else -> getPhysicalDeviceName()
            }.ifBlank { getString("panel.device.label.device_unknown") }
        } catch (e: Exception) {
            getString("panel.device.label.device_unknown")
        }

    private fun getEmulatorName(): String {
        return underlyingDevice.avdData.get().name?.replace("_", " ").orEmpty()
    }

    private fun getPhysicalDeviceName(): String {
        val manufacturer = (underlyingDevice.getProperty("ro.product.brand") ?: "").lowercase()
        val model = (underlyingDevice.getProperty(IDevice.PROP_DEVICE_MODEL) ?: "").lowercase()

        return when {
            model.startsWith(manufacturer) -> model
            else -> "$manufacturer $model"
        }.trim()
            .split(" ")
            .joinToString(" ") { it.capitalizeUS() }
    }

    fun fetchDeviceInfo(): Observable<MainDeviceDetails> {
        val observable = BehaviorSubject.create<MainDeviceDetails>()
        CoroutineScope(Job() + Dispatchers.IO).launch {
            val name = waitForValidValue { displayName }
            val api = waitForValidValue { androidVersion }
            val sdk = waitForValidValue { sdkVersion }

            retrieveInstalledPackages { packages ->
                observable.onNext(
                    MainDeviceDetails(name, sdk, api, packages, isWearable)
                )
            }
        }
        return observable
    }

    private suspend fun waitForValidValue(provider: () -> String): String {
        var value = provider()
        while (value.isBlank() || value == getString("panel.device.label.device_unknown")) {
            delay(100)
            value = provider()
        }
        return value
    }

    private fun retrieveInstalledPackages(callback: (List<String>) -> Unit) {
        val output = StringBuilder()
        runCatching {
            underlyingDevice.executeShellCommand("pm list packages", object : IShellOutputReceiver {
                override fun addOutput(data: ByteArray, offset: Int, length: Int) {
                    output.append(String(data, offset, length, StandardCharsets.UTF_8))
                }

                override fun flush() {
                    callback(output.split("\n"))
                }

                override fun isCancelled() = false
            })
        }.onException(
            AdbCommandRejectedException::class,
            java.net.SocketException::class
        ) {
            callback(emptyList())
        }
    }

    fun installTalkBackForDevelopers(): Observable<Boolean> {
        val apkFile = if (isWearable) APK_WEAR else APK_PHONE
        return installApplication(apkFile)
    }

    fun executeScript(command: String) {
        underlyingDevice.executeShellCommand(command, NullOutputReceiver())
    }

    private fun installApplication(apkResourceName: String): Observable<Boolean> {
        val result = BehaviorSubject.create<Boolean>()
        CoroutineScope(Job() + Dispatchers.IO).launch {
            try {
                val tempApk = extractResourceToTempFile("/files/$apkResourceName")
                installToDevice(tempApk, result)
            } catch (e: FileNotFoundException) {
                result.onNext(false)
            }
        }
        return result
    }

    private fun extractResourceToTempFile(resourcePath: String): File {
        val input = Device::class.java.getResourceAsStream(resourcePath)
            ?: throw FileNotFoundException("Resource not found: $resourcePath")
        val tempFile = File.createTempFile("temp_apk", ".apk")
        Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        return tempFile
    }

    private fun installToDevice(apkFile: File, result: BehaviorSubject<Boolean>) {
        try {
            underlyingDevice.installPackage(apkFile.absolutePath, true, object : InstallReceiver() {
                override fun done() {
                    apkFile.delete()
                    result.onNext(isSuccessfullyCompleted)
                }
            })
        } catch (e: InstallException) {
            result.onNext(false)
        } catch (e: Exception) {
            result.onNext(false)
        }
    }
}