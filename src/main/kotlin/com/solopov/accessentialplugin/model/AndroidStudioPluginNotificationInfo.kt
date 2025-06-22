package com.solopov.accessentialplugin.model

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction

data class AndroidStudioPluginNotificationInfo(
    val title: String,
    val message: String,
    val actionsList: Collection<AnAction> = listOf(),
    val notificationType: NotificationType = NotificationType.INFORMATION,
) : NotificationInfo