package com.student.qujian.model

import kotlinx.serialization.Serializable

/** 取件来源平台 */
@Serializable
enum class Platform(val label: String) {
    PINDUODUO("拼多多"),
    TAOBAO("淘宝"),
    JD("京东"),
    CAINIAO("菜鸟"),
    FENGCHAO("丰巢"),
    OTHER("其他");

    companion object {
        /** 根据通知包名推断平台 */
        fun fromPackage(pkg: String?): Platform = when {
            pkg == null -> OTHER
            pkg.contains("pinduoduo") || pkg.contains("yangkeduo") -> PINDUODUO
            pkg.contains("taobao") || pkg.contains("tmall") || pkg.contains("cainiao") -> TAOBAO
            pkg.contains("jd.") || pkg.contains("jingdong") -> JD
            else -> OTHER
        }

        /** 根据文本关键词推断平台（无包名时用） */
        fun fromText(text: String): Platform? = when {
            text.contains("拼多多") -> PINDUODUO
            text.contains("淘宝") || text.contains("天猫") -> TAOBAO
            text.contains("京东") -> JD
            text.contains("菜鸟") -> CAINIAO
            text.contains("丰巢") -> FENGCHAO
            else -> null
        }
    }
}

/** 一条取件记录 */
@Serializable
data class Pickup(
    val id: String,
    val code: String,
    val platform: Platform,
    val station: String? = null,
    val cabinet: String? = null, // 旧版单级柜号，保留兼容历史数据
    val area: String? = null,    // 区域类型：室外快递柜 / 室内货架区
    val spot: String? = null,    // 具体位置：5号柜 / 009货架
    val rawText: String,
    val timestamp: Long,
    val isIdentityCode: Boolean = false,
    val isRead: Boolean = false
) {
    /** 区域分组键 */
    val areaKey: String get() = area ?: "其他"
    /** 位置分组键 */
    val spotKey: String get() = spot ?: "其他"
    /** 用于展示的简短描述 */
    val timeLabel: String get() = timestamp.toTimeLabel()
}

/** 区域排序：室外柜 → 室内货架 → 其他 */
val AREA_ORDER = listOf("室外快递柜", "室内货架区", "其他")

fun areaOrderKey(area: String): Int = AREA_ORDER.indexOf(area).let { if (it < 0) AREA_ORDER.size else it }

private fun Long.toTimeLabel(): String {
    val diff = System.currentTimeMillis() - this
    return when {
        diff < 60_000 -> "刚刚"
        diff < 3_600_000 -> "${diff / 60_000} 分钟前"
        diff < 86_400_000 -> "${diff / 3_600_000} 小时前"
        diff < 172_800_000 -> "昨天"
        else -> "${diff / 86_400_000} 天前"
    }
}
