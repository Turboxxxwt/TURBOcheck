package com.student.qujian.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.student.qujian.core.CodeParser
import com.student.qujian.core.PickupStore

/**
 * 通知监听服务：系统把每一条通知推送过来，我们只做一件事——
 * 从快递类 App 的通知里提取取件码，存到本地。
 *
 * 不修改、不拦截、不删除任何通知，不上传任何数据。
 */
class PickupNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "通知监听已连接")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val notification = sbn ?: return
        if (notification.packageName !in PickupStore.enabledPackages.value) return

        val extras = notification.notification.extras ?: return
        val parts = mutableListOf<String>()
        listOf(
            Notification.EXTRA_TITLE,
            Notification.EXTRA_TITLE_BIG,
            Notification.EXTRA_TEXT,
            Notification.EXTRA_BIG_TEXT,
            Notification.EXTRA_SUMMARY_TEXT
        ).forEach { key ->
            extras.getCharSequence(key)?.toString()?.let { parts.add(it) }
        }
        extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.forEach {
            parts.add(it.toString())
        }
        val content = parts.filter { it.isNotBlank() }.joinToString(" ")

        val result = CodeParser.parse(content, notification.packageName) ?: return
        val added = PickupStore.add(result, content)
        Log.i(TAG, "识别到取件码: ${result.code} (${result.platform?.label}), 新增=$added")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // 用户取件后通知消失，但我们不自动删除记录——让用户自己确认已取件
    }

    companion object {
        private const val TAG = "QujianListener"
    }
}
