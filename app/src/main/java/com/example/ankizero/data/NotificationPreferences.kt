package com.example.ankizero.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object NotificationPreferences {
    val DAILY_REMINDERS_ENABLED = booleanPreferencesKey("daily_reminders_enabled")
    val REMINDER_TIME_HOUR = intPreferencesKey("reminder_time_hour")
    val REMINDER_TIME_MINUTE = intPreferencesKey("reminder_time_minute")
}
