package com.student.qujian.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** iOS inset grouped 分组卡片：10dp 圆角，secondarySystemGroupedBackground 底色 */
@Composable
fun IosCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface),
        content = content
    )
}

/** iOS 分组标题：13pt、次要色、左对齐 */
@Composable
fun IosSectionHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        color = Subtle
    )
}

/** iOS 分组内分隔线：左侧内缩，0.5pt */
@Composable
fun IosDivider(startPadding: Dp = 16.dp) {
    HorizontalDivider(
        modifier = Modifier.padding(start = startPadding),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline
    )
}

/** iOS 标准列表行：17pt 标题，可选前置图标与后置控件 */
@Composable
fun IosRow(
    title: String,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Normal,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    minHeight: Dp = 52.dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .then(if (onClick != null) Modifier.bouncyClick(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 17.sp,
            fontWeight = fontWeight,
            color = titleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (trailing != null) {
            Spacer(Modifier.width(10.dp))
            trailing()
        }
    }
}

/** iOS 圆形图标按钮：浅色底 + 系统蓝图标 */
@Composable
fun IosCircleButton(icon: ImageVector, onClick: () -> Unit, description: String? = null) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .bouncyClick(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = Accent, modifier = Modifier.size(22.dp))
    }
}

/** iOS 右箭头（disclosure indicator） */
@Composable
fun IosChevron() {
    Icon(
        Icons.AutoMirrored.Outlined.KeyboardArrowRight,
        contentDescription = null,
        tint = Subtle,
        modifier = Modifier.size(20.dp)
    )
}

/** iOS 风格开关：开启为系统绿 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IosSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)? = null) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = IosGreen,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color(0xFFE5E5EA),
            checkedBorderColor = Color.Transparent,
            uncheckedBorderColor = Color.Transparent
        )
    )
}
