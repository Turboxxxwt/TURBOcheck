package com.student.qujian.core

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.student.qujian.service.PickupNotificationListener

object NotificationHelper {

    /** 检查用户是否已在系统设置中授予通知读取权限 */
    fun isGranted(context: Context): Boolean {
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        val target = ComponentName(context, PickupNotificationListener::class.java).flattenToString()
        return flat.split(":").any { it == target }
    }

    /** 跳转到系统「通知使用权」设置页 */
    fun openSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
