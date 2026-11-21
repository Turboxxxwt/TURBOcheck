package com.student.qujian.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.student.qujian.core.AppLinks
import com.student.qujian.core.CodeParser
import com.student.qujian.core.NotificationHelper
import com.student.qujian.core.PickupStore
import com.student.qujian.model.Pickup
import com.student.qujian.model.Platform
import com.student.qujian.model.areaOrderKey
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onSettings: () -> Unit) {
    val pickups by PickupStore.pickups.collectAsState()
    val context = LocalContext.current
    var granted by remember { mutableStateOf(NotificationHelper.isGranted(context)) }
    var showImportSms by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { granted = NotificationHelper.isGranted(context) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // 关键修复：edge-to-edge 下为状态栏/中置挖孔让位，标题不再被遮挡
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "TURBO查件",
                    modifier = Modifier.weight(1f),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IosCircleButton(Icons.Outlined.Add, onClick = { showImportSms = true }, description = "导入短信")
                Spacer(Modifier.width(12.dp))
                IosCircleButton(Icons.Outlined.Settings, onClick = onSettings, description = "设置")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            IosSectionHeader("身份码")
            IosCard {
                IosRow(
                    title = "拼多多",
                    leading = { PlatformDot(Platform.PINDUODUO) },
                    trailing = { IosChevron() },
                    onClick = {
                        if (!AppLinks.openPdd(context)) {
                            scope.launch { snackbar.showSnackbar("未安装拼多多") }
                        }
                    }
                )
                IosDivider()
                IosRow(
                    title = "菜鸟驿站",
                    leading = { PlatformDot(Platform.CAINIAO) },
                    trailing = { IosChevron() },
                    onClick = {
                        if (!AppLinks.openTaobaoCainiao(context)) {
                            scope.launch { snackbar.showSnackbar("未安装淘宝") }
                        }
                    }
                )
                IosDivider()
                IosRow(
                    title = "近邻宝",
                    leading = { PlatformDot(Platform.FENGCHAO) },
                    trailing = { IosChevron() },
                    onClick = {
                        when (AppLinks.openJinlinbao(context)) {
                            AppLinks.JinlinbaoResult.OPENED_APP -> Unit
                            AppLinks.JinlinbaoResult.OPENED_WECHAT ->
                                scope.launch { snackbar.showSnackbar("请在微信搜索「近邻宝」") }
                            AppLinks.JinlinbaoResult.NOT_INSTALLED ->
                                scope.launch { snackbar.showSnackbar("未安装微信或近邻宝") }
                        }
                    }
                )
            }

            Spacer(Modifier.height(28.dp))

            IosSectionHeader("取件码")
            when {
                pickups.isEmpty() && !granted -> IosCard {
                    IosRow(
                        title = "开启通知读取",
                        titleColor = Accent,
                        trailing = { IosChevron() },
                        onClick = {
                            NotificationHelper.openSettings(context)
                            granted = true
                        }
                    )
                }

                pickups.isEmpty() -> Unit

                else -> {
                    // 两级智能分类：区域类型（室外快递柜 / 室内货架区）→ 具体位置（5号柜 / 009货架）
                    val onDone: (Pickup) -> Unit = { item ->
                        PickupStore.remove(item.id)
                        scope.launch {
                            val result = snackbar.showSnackbar("已移除", "撤销")
                            if (result == SnackbarResult.ActionPerformed) {
                                PickupStore.restore(item)
                            }
                        }
                    }
                    val byArea = pickups.groupBy { it.areaKey }
                    val areas = byArea.keys.sortedWith(
                        compareBy<String> { areaOrderKey(it) }.thenBy { it }
                    )
                    areas.forEachIndexed { aIndex, area ->
                        IosSectionHeader(area)
                        val bySpot = byArea.getValue(area).groupBy { it.spotKey }
                        val spots = bySpot.keys.sortedWith(
                            compareBy<String> { it == "其他" }.thenBy { it }
                        )
                        spots.forEachIndexed { sIndex, spot ->
                            // 位置子标题
                            Text(
                                spot,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 8.dp),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            IosCard {
                                val items = bySpot.getValue(spot)
                                items.forEachIndexed { index, item ->
                                    // 关键：用 id 作为 key，避免左滑删除后位置复用导致滑动状态残留
                                    key(item.id) {
                                        DismissiblePickupRow(item = item, onDone = { onDone(item) })
                                    }
                                    if (index < items.lastIndex) IosDivider()
                                }
                            }
                            if (sIndex < spots.lastIndex) Spacer(Modifier.height(14.dp))
                        }
                        if (aIndex < areas.lastIndex) Spacer(Modifier.height(20.dp))
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    if (showImportSms) ImportSmsDialog(
        onDismiss = { showImportSms = false },
        onImported = { added, duplicated ->
            val msg = when {
                added > 0 && duplicated > 0 -> "已导入 $added 条，跳过重复 $duplicated 条"
                added > 0 -> "已导入 $added 条取件码"
                duplicated > 0 -> "重复，未导入"
                else -> "未识别到取件码"
            }
            scope.launch { snackbar.showSnackbar(msg) }
        }
    )
}

@Composable
private fun PlatformDot(platform: Platform) {
    Box(Modifier.size(10.dp).clip(CircleShape).background(platformColor(platform)))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DismissiblePickupRow(item: Pickup, onDone: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { it != SwipeToDismissBoxValue.Settled },
        positionalThreshold = { total -> total * 0.35f }
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) onDone()
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val fraction = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                dismissState.progress
            } else 0f
            Row(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(IosGreen.copy(alpha = 0.15f + 0.5f * fraction)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    Icons.Outlined.Check,
                    contentDescription = "完成",
                    tint = IosGreen,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        PickupRow(item, onDone)
    }
}

@Composable
private fun PickupRow(item: Pickup, onDone: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlatformDot(item.platform)
        Spacer(Modifier.width(14.dp))
        Text(
            item.code,
            Modifier.weight(1f),
            fontFamily = FontFamily.Monospace,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .bouncyClick(onClick = onDone),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = "完成",
                tint = Accent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun platformColor(platform: Platform): Color = when (platform) {
    Platform.PINDUODUO -> Color(0xFFE02E24)
    Platform.TAOBAO -> Color(0xFFFF5000)
    Platform.JD -> Color(0xFFE1251B)
    Platform.CAINIAO -> Color(0xFF1E9E55)
    Platform.FENGCHAO -> Color(0xFF2B7FFF)
    Platform.OTHER -> Subtle
}

/** 手动导入短信：粘贴一条或多条短信（空行分隔），逐条解析入库。 */
@Composable
private fun ImportSmsDialog(
    onDismiss: () -> Unit,
    onImported: (added: Int, duplicated: Int) -> Unit
) {
    var text by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
        ) {
            Text(
                "导入取件短信",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "粘贴短信内容，多条请用空行分隔",
                fontSize = 13.sp,
                color = Subtle
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("取件码 ...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                shape = RoundedCornerShape(10.dp),
                maxLines = 12
            )
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("取消", color = Subtle) }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = {
                    val blocks = text
                        .split(Regex("\\n\\s*\\n"))
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    var added = 0
                    var duplicated = 0
                    for (block in blocks) {
                        val result = CodeParser.parse(block) ?: continue
                        if (PickupStore.add(result, block)) added++ else duplicated++
                    }
                    onDismiss()
                    onImported(added, duplicated)
                }) {
                    Text("导入", color = Accent, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
