package com.student.qujian.ui

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// iOS 系统色板
val Ink = Color(0xFF000000)            // label
val Subtle = Color(0xFF8E8E93)         // secondaryLabel
val Grouped = Color(0xFFF2F2F7)        // systemGroupedBackground（页面底色）
val Card = Color(0xFFFFFFFF)           // secondarySystemGroupedBackground（分组卡片）
val CardDark = Color(0xFF1C1C1E)
val Accent = Color(0xFF007AFF)         // systemBlue
val LineColor = Color(0xFFE5E5EA)      // separator
val IosGreen = Color(0xFF34C759)
val IosRed = Color(0xFFFF3B30)
val IosOrange = Color(0xFFFF9500)

private val LightColors = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    background = Grouped,
    onBackground = Ink,
    surface = Card,
    onSurface = Ink,
    secondary = Subtle,
    outline = LineColor
)

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    background = Color(0xFF000000),
    onBackground = Color.White,
    surface = CardDark,
    onSurface = Color.White,
    secondary = Color(0xFF98989D),
    outline = Color(0xFF38383A)
)

@Composable
fun QujianTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val colors = if (dark) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // 边到边：内容铺到状态栏/手势栏，由 Composable 用 insets 让开安全区
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            // 中置挖孔状态栏：亮色主题用深色图标，保证可读
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }
    MaterialTheme(colorScheme = colors, content = content)
}
