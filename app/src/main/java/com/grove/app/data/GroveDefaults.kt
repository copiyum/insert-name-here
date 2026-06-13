package com.grove.app.data

import com.grove.app.data.model.NotificationSettings

object GroveDefaults {
    const val DEFAULT_CURRENCY = "INR"
    const val DEFAULT_USER_NAME = "Mae"
    const val DEFAULT_NOTIFICATION_MINUTES = 480
    const val MAX_USER_NAME_LENGTH = 30
    const val TOAST_DURATION_MS = 1800L

    val DEFAULT_NOTIFICATION_SETTINGS = NotificationSettings(true, DEFAULT_NOTIFICATION_MINUTES, true, 3)
}
