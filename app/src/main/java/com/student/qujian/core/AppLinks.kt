package com.student.qujian.core

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * 外部跳转集中管理。
 *
 * 这些 scheme 是平台私有的，不是官方 API，可能随 App 版本变动而失效。
 * 集中放在这里，失效时只改对应常量即可。
 * 跳转失败返回 false / NOT_INSTALLED，由调用方给出反馈。
 */
object AppLinks {
    /** 拼多多「取快递-扫码亮灯」身份码页 */
    private const val PDD = "pinduoduo://com.xunmeng.pinduoduo/mdkd/package"

    /** 淘宝内菜鸟驿站身份码页（当前登录账号的动态码） */
    private const val TAOBAO_CAINIAO =
        "taobao://m.taobao.com/tbopen/index.html?h5Url=" +
            "https://pages-fast.m.taobao.com/wow/z/uniapp/1011717/" +
            "last-mile-fe/end-collect-platform/identity-code"

    /** 近邻宝独立 App 包名（用户版，非快递员版） */
    private const val JINLINBAO_PKG = "com.jlb.mobile"

    /** 微信包名，用于小程序跳转回退 */
    private const val WECHAT_PKG = "com.tencent.mm"

    fun openPdd(context: Context): Boolean = open(context, PDD)

    fun openTaobaoCainiao(context: Context): Boolean = open(context, TAOBAO_CAINIAO)

    /**
     * 跳转近邻宝取快递界面。
     *
     * 「近邻宝」小程序的 AppID 在公开渠道是脱敏的，微信明文 scheme
     * (weixin://dl/business/?appid=...&path=...) 需要完整 AppID 与已发布页面 path，
     * 无法可靠构造，因此不猜测、不使用来路不明的 ticket。
     * 采用两级稳妥策略：
     *   1. 已装近邻宝 App → 直接打开取件界面；
     *   2. 否则拉起微信 → 提示用户在微信搜索「近邻宝」小程序。
     */
    fun openJinlinbao(context: Context): JinlinbaoResult {
        if (launchPackage(context, JINLINBAO_PKG)) return JinlinbaoResult.OPENED_APP
        if (launchPackage(context, WECHAT_PKG)) return JinlinbaoResult.OPENED_WECHAT
        return JinlinbaoResult.NOT_INSTALLED
    }

    enum class JinlinbaoResult { OPENED_APP, OPENED_WECHAT, NOT_INSTALLED }

    private fun launchPackage(context: Context, pkg: String): Boolean = try {
        val intent = context.packageManager.getLaunchIntentForPackage(pkg)
            ?: return false
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: Exception) {
        false
    }

    private fun open(context: Context, url: String): Boolean = try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
