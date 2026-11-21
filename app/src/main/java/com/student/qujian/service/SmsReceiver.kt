package com.student.qujian.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.student.qujian.core.CodeParser
import com.student.qujian.core.PickupStore

/**
 * 短信兜底：部分驿站仍通过短信发送取件码。
 * 仅在用户开启「短信识别」后生效，且只解析、不存储原始短信全文以外的内容。
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        if (!PickupStore.smsEnabled.value) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val text = messages.joinToString("") { it.displayMessageBody }
        if (text.isBlank()) return

        val result = CodeParser.parse(text) ?: return
        val added = PickupStore.add(result, text)
        Log.i(TAG, "短信识别到取件码: ${result.code}, 新增=$added")
    }

    companion object {
        private const val TAG = "QujianSms"
    }
}
