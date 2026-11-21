package com.student.qujian.ui

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Apple 流体交互参数，对齐 WWDC：
 * - 普通 UI：阻尼比 1.0（临界阻尼，无弹跳）
 * - 手势抛动 / 抽屉：阻尼比 0.8（轻微弹跳）
 * 弹簧没有固定时长，靠物理趋近目标，正好匹配一加 15 的 165Hz 高刷屏。
 */
object Motion {
    fun <T> normal(reduce: Boolean): FiniteAnimationSpec<T> =
        if (reduce) snap() else spring(dampingRatio = 1.0f, stiffness = 260f)

    fun <T> bouncy(reduce: Boolean): FiniteAnimationSpec<T> =
        if (reduce) snap() else spring(dampingRatio = 0.8f, stiffness = 320f)

    fun <T> press(reduce: Boolean): FiniteAnimationSpec<T> =
        if (reduce) snap() else spring(dampingRatio = 1.0f, stiffness = 520f)
}

/** 尊重系统「动画时长比例」：用户开了减弱动效（值为 0）时关闭动画 */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        }.getOrDefault(false)
    }
}

/**
 * 按下即反馈：弹簧缩放 + 轻触觉；抬起时从实时位置回弹，可随时打断。
 * 把「按压反馈」收敛成一个 modifier，界面里不再散落动画代码。
 */
@Composable
fun Modifier.bouncyClick(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    val haptic = LocalHapticFeedback.current
    val reduce = rememberReduceMotion()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }

    LaunchedEffect(pressed) {
        if (!reduce) {
            scale.animateTo(if (pressed) 0.96f else 1f, animationSpec = Motion.press(reduce))
        }
    }

    return this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled
        ) {
            if (!reduce) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            onClick()
        }
}
