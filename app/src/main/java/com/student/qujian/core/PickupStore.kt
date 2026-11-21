package com.student.qujian.core

import android.content.Context
import android.content.SharedPreferences
import com.student.qujian.model.Pickup
import com.student.qujian.model.Platform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * 本地仓库：所有数据只存在用户手机上，不联网、不上传。
 * 用 StateFlow 驱动 UI，无需数据库，保持足够简单。
 */
object PickupStore {

    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var prefs: SharedPreferences

    val defaultPackages = setOf(
        "com.xunmeng.pinduoduo",      // 拼多多
        "com.taobao.taobao",          // 淘宝
        "com.tmall.wireless",         // 天猫
        "com.jingdong.app.mall",      // 京东
        "com.cainiao.wireless",       // 菜鸟
        "com.fcbox.hive",             // 丰巢
        "com.sf.express"              // 顺丰
    )

    private val _pickups = MutableStateFlow<List<Pickup>>(emptyList())
    val pickups: StateFlow<List<Pickup>> = _pickups.asStateFlow()

    private val _enabledPackages = MutableStateFlow<Set<String>>(defaultPackages)
    val enabledPackages: StateFlow<Set<String>> = _enabledPackages.asStateFlow()

    private val _smsEnabled = MutableStateFlow(false)
    val smsEnabled: StateFlow<Boolean> = _smsEnabled.asStateFlow()

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences("qujian", Context.MODE_PRIVATE)

        prefs.getString("pickups", null)?.let { raw ->
            runCatching {
                _pickups.value = json.decodeFromString<List<Pickup>>(raw)
            }
        }
        prefs.getStringSet("enabled_packages", null)?.let {
            _enabledPackages.value = it
        }
        _smsEnabled.value = prefs.getBoolean("sms_enabled", false)
    }

    /**
     * 新增一条取件码。5 分钟内同平台同码视为重复，避免通知更新导致刷屏。
     */
    fun add(result: CodeParser.Result, rawText: String): Boolean {
        val now = System.currentTimeMillis()
        val duplicate = _pickups.value.any { existing ->
            existing.code == result.code &&
                existing.platform == (result.platform ?: Platform.OTHER) &&
                now - existing.timestamp < 5 * 60 * 1000L
        }
        if (duplicate) return false

        val item = Pickup(
            id = UUID.randomUUID().toString(),
            code = result.code,
            platform = result.platform ?: Platform.OTHER,
            station = result.station,
            cabinet = result.cabinet,
            area = result.area,
            spot = result.spot,
            rawText = rawText,
            timestamp = now,
            isIdentityCode = result.isIdentityCode
        )
        _pickups.value = listOf(item) + _pickups.value
        persist()
        return true
    }

    /** 手动添加（无解析结果时） */
    fun addManual(code: String, platform: Platform, station: String?, identity: Boolean = false) {
        val item = Pickup(
            id = UUID.randomUUID().toString(),
            code = code.trim(),
            platform = platform,
            station = station?.trim()?.takeIf { it.isNotEmpty() },
            rawText = "手动添加",
            timestamp = System.currentTimeMillis(),
            isIdentityCode = identity
        )
        _pickups.value = listOf(item) + _pickups.value
        persist()
    }

    fun remove(id: String) {
        _pickups.value = _pickups.value.filterNot { it.id == id }
        persist()
    }

    /** 撤销删除：把原记录按原时间放回 */
    fun restore(item: Pickup) {
        if (item.id in _pickups.value.map { it.id }) return
        _pickups.value = (listOf(item) + _pickups.value).sortedByDescending { it.timestamp }
        persist()
    }

    fun markRead(id: String) {
        _pickups.value = _pickups.value.map { if (it.id == id) it.copy(isRead = true) else it }
        persist()
    }

    fun clear() {
        _pickups.value = emptyList()
        persist()
    }

    fun togglePackage(pkg: String) {
        val current = _enabledPackages.value
        _enabledPackages.value = if (pkg in current) current - pkg else current + pkg
        prefs.edit().putStringSet("enabled_packages", _enabledPackages.value).apply()
    }

    fun setSmsEnabled(enabled: Boolean) {
        _smsEnabled.value = enabled
        prefs.edit().putBoolean("sms_enabled", enabled).apply()
    }

    private fun persist() {
        prefs.edit().putString("pickups", json.encodeToString(_pickups.value)).apply()
    }
}
