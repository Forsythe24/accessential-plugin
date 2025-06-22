package com.solopov.accessentialplugin.model

data class MainService(
    val displayNameKey: String,
    val componentPackage: String,
    val serviceComponent: String
)

val allSupportedServices = buildList {
    add(
        MainService(
            "android.accessibility.service.name.accessibility-menu",
            "com.android.systemui.accessibility.accessibilitymenu",
            "com.android.systemui.accessibility.accessibilitymenu.AccessibilityMenuService"
        )
    )
    add(
        MainService(
            "android.accessibility.service.name.voice-access",
            "com.google.android.apps.accessibility.voiceaccess",
            "com.google.android.apps.accessibility.voiceaccess.JustSpeakService"
        )
    )
    add(
        MainService(
            "android.accessibility.service.name.talkback-for-developers",
            "com.android.talkback4d",
            "com.developer.talkback.TalkBackDevService"
        )
    )
    add(
        MainService(
            "android.accessibility.service.name.accessibility-scanner",
            "com.google.android.apps.accessibility.auditor",
            "com.google.android.apps.accessibility.auditor.ScannerService"
        )
    )
    add(
        MainService(
            "android.accessibility.service.name.switch-access",
            "com.google.android.accessibility.switchaccess",
            "com.android.switchaccess.SwitchAccessService"
        )
    )
    add(
        MainService(
            "android.accessibility.service.name.display-service",
            "com.solopov.accessential.teaching",
            "com.solopov.accessential.teaching.DisplayAccessibilityService"
        )
    )
}

fun findMatchingServices(installedPackages: List<String>): List<MainService> {
    val packagePrefix = "package:"
    val installedPackageNames = installedPackages
        .filter { it.startsWith(packagePrefix) }
        .map { it.removePrefix(packagePrefix) }

    return allSupportedServices.filter { service ->
        service.componentPackage in installedPackageNames
    }
}