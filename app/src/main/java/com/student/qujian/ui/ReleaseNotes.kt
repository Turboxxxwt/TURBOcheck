package com.student.qujian.ui

/**
 * 更新公告数据源：每个版本的公告均保留，按版本号降序排列（最新在前）。
 * 每次发版：在列表顶部新增一条，并把 build.gradle.kts 的版本号同步递增。
 */
data class ReleaseNote(
    val version: String,
    val date: String,
    val summary: String
)

object ReleaseNotes {
    /** 全部历史版本，按版本号从高到低排列 */
    val history: List<ReleaseNote> = listOf(
        ReleaseNote(
            version = "1.5",
            date = "2026-11-21",
            summary = "更新公告整合至设置独立入口；优化启动体验，系统启动窗口与开屏动画视觉统一、衔接更流畅。"
        ),
        ReleaseNote(
            version = "1.4",
            date = "2026-11-21",
            summary = "更新公告整合为一张卡片集中展示；优化开屏显示，品牌名与图标同步呈现，视觉更干净。"
        ),
        ReleaseNote(
            version = "1.3",
            date = "2026-11-21",
            summary = "更新公告支持查看全部历史版本并标注更新时间；重新设计开屏动画，过渡更流畅、更具层次感。"
        ),
        ReleaseNote(
            version = "1.2",
            date = "2026-11-21",
            summary = "缩短开屏停留时间，进入应用更迅速；修复移除取件码后条目残留的问题，列表交互更稳定。"
        ),
        ReleaseNote(
            version = "1.1",
            date = "2026-11-21",
            summary = "优化近邻宝取件短信识别，新增开屏动画与更新公告，并将添加取件码入口整合为手动导入短信。"
        ),
        ReleaseNote(
            version = "1.0",
            date = "2026-11-21",
            summary = "首个版本发布，支持拼多多、淘宝、京东等平台取件码智能识别与身份码一键跳转。"
        )
    )
}
