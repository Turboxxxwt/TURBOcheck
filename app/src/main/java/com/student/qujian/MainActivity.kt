package com.student.qujian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import com.student.qujian.ui.AboutScreen
import com.student.qujian.ui.HomeScreen
import com.student.qujian.ui.QujianTheme
import com.student.qujian.ui.ReleaseNotesScreen
import com.student.qujian.ui.SettingsScreen
import com.student.qujian.ui.SplashScreen
import com.student.qujian.ui.rememberReduceMotion

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 必须先于 super.onCreate：用定制的启动窗口替代默认白底+白方块图标
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            QujianTheme {
                AppContent()
            }
        }
    }
}

@Composable
private fun AppContent() {
    var screen by remember { mutableStateOf(Screen.Home) }
    var showSplash by remember { mutableStateOf(true) }
    val reduce = rememberReduceMotion()

    Box(Modifier.fillMaxSize()) {
        // 主界面作为底层，开屏淡出后自然露出
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                // iOS 导航转场：两侧同步移动、无回弹、带视差。
                // 旧实现让一侧 tween(0) 瞬间跳变 + 另一侧 bouncy 回弹，返回时会卡顿/抖动。
                val forward = targetState.ordinal > initialState.ordinal
                val spec = if (reduce) {
                    tween<IntOffset>(durationMillis = 0)
                } else {
                    tween<IntOffset>(durationMillis = 260, easing = FastOutSlowInEasing)
                }
                if (forward) {
                    // 进入：新页从右侧整屏滑入，旧页向左轻微视差退出
                    (slideInHorizontally(animationSpec = spec) { it }) togetherWith
                        (slideOutHorizontally(animationSpec = spec) { -it / 4 })
                } else {
                    // 返回：新页（主界面）从左侧视差滑入，旧页向右整屏退出
                    (slideInHorizontally(animationSpec = spec) { -it / 4 }) togetherWith
                        (slideOutHorizontally(animationSpec = spec) { it })
                }
            },
            label = "screen"
        ) { target ->
            when (target) {
                Screen.Home -> HomeScreen(onSettings = { screen = Screen.Settings })
                Screen.Settings -> SettingsScreen(
                    onBack = { screen = Screen.Home },
                    onAbout = { screen = Screen.About },
                    onReleaseNotes = { screen = Screen.ReleaseNotes }
                )
                Screen.About -> AboutScreen(onBack = { screen = Screen.Home })
                Screen.ReleaseNotes -> ReleaseNotesScreen(onBack = { screen = Screen.Home })
            }
        }

        // 开屏覆盖在最上层：品牌动画结束后淡出，露出主界面
        AnimatedVisibility(
            visible = showSplash,
            exit = fadeOut(animationSpec = tween(if (reduce) 0 else 220))
        ) {
            SplashScreen(reduce = reduce, onFinished = { showSplash = false })
        }
    }

    // 系统返回键：设置/关于页一律回到主界面，不退出 App
    BackHandler(enabled = !showSplash && screen != Screen.Home) {
        screen = Screen.Home
    }
}

private enum class Screen { Home, Settings, About, ReleaseNotes }
