package com.solopov.accessentialplugin.model

data class MainDeviceDetails(
    val name: String,
    val sdkLevel: String,
    val api: String,
    val packageList: List<String>,
    val isWatch: Boolean,
)