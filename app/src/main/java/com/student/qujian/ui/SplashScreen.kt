package com.student.qujian.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.student.qujian.R
import kotlinx.coroutines.delay

/**
 * 开屏动画（iOS 风格）：
 * 系统启动窗口已展示同款图标、同色背景，这里让图标保持可见并做弹性缩放归位，
 * 品牌名随后淡入上浮——与系统启动窗口无缝衔接，不会出现「图标先消失再淡入」的跳变。
 * 遵从系统「减弱动效」时静态停留。
 */
@Composable
fun SplashScreen(reduce: Boolean, onFinished: () -> Unit) {
    var started by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        started = true
        delay(if (reduce) 160L else 1050L)
        onFinished()
    }

    // 图标：始终可见，缩放归位 + 轻微上浮（衔接系统启动窗口的静态图标）
    val icon by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = if (reduce) tween(0) else tween(560, easing = FastOutSlowInEasing),
        label = "splash-icon"
    )
    // 品牌名：随后淡入
    val text by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = if (reduce) {
            tween(0)
        } else {
            tween(520, delayMillis = 120, easing = FastOutSlowInEasing)
        },
        label = "splash-text"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher),
                contentDescription = "TURBO查件图标",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .scale(1.12f - 0.12f * icon)
                    .offset(y = ((1f - icon) * 8).dp)
            )

            Spacer(Modifier.height(22.dp))
            Text(
                "TURBO查件",
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .alpha(text)
                    .offset(y = ((1f - text) * 8).dp)
            )
        }
    }
}
