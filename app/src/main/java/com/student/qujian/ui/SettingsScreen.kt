package com.student.qujian.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.student.qujian.core.NotificationHelper
import com.student.qujian.core.PickupStore

private data class AppEntry(val pkg: String, val name: String)

private val apps = listOf(
    AppEntry("com.xunmeng.pinduoduo", "拼多多"),
    AppEntry("com.taobao.taobao", "淘宝"),
    AppEntry("com.tmall.wireless", "天猫"),
    AppEntry("com.jingdong.app.mall", "京东"),
    AppEntry("com.cainiao.wireless", "菜鸟"),
    AppEntry("com.fcbox.hive", "丰巢"),
    AppEntry("com.sf.express", "顺丰")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onAbout: () -> Unit,
    onReleaseNotes: () -> Unit
) {
    val context = LocalContext.current
    val granted = NotificationHelper.isGranted(context)
    val sms by PickupStore.smsEnabled.collectAsState()
    val enabled by PickupStore.enabledPackages.collectAsState()

    val smsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { ok -> PickupStore.setSmsEnabled(ok) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // 关键修复：edge-to-edge 下为状态栏让位
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp)
                    .padding(top = 4.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "返回",
                        tint = Accent
                    )
                }
                Text(
                    "设置",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
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
            Spacer(Modifier.height(8.dp))

            IosCard {
                IosRow(
                    title = "更新公告",
                    trailing = { IosChevron() },
                    onClick = onReleaseNotes
                )
            }

            Spacer(Modifier.height(24.dp))

            IosCard {
                IosRow(
                    title = "通知读取",
                    trailing = {
                        if (granted) {
                            Icon(Icons.Outlined.Check, null, tint = Accent)
                        } else {
                            IosChevron()
                        }
                    },
                    onClick = { NotificationHelper.openSettings(context) }
                )
            }

            Spacer(Modifier.height(24.dp))

            IosCard {
                IosRow(
                    title = "短信",
                    trailing = { IosSwitch(sms, null) },
                    onClick = {
                        if (sms) PickupStore.setSmsEnabled(false)
                        else smsLauncher.launch(Manifest.permission.RECEIVE_SMS)
                    }
                )
            }

            Spacer(Modifier.height(24.dp))

            IosSectionHeader("读取来源")
            IosCard {
                apps.forEachIndexed { index, app ->
                    IosRow(
                        title = app.name,
                        trailing = { IosSwitch(app.pkg in enabled, null) },
                        onClick = { PickupStore.togglePackage(app.pkg) }
                    )
                    if (index < apps.lastIndex) IosDivider()
                }
            }

            Spacer(Modifier.height(24.dp))

            IosCard {
                IosRow(
                    title = "关于APP",
                    trailing = { IosChevron() },
                    onClick = onAbout
                )
            }

            Spacer(Modifier.height(24.dp))

            IosCard {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .bouncyClick(onClick = { PickupStore.clear() }),
                    contentAlignment = Alignment.Center
                ) {
                    Text("清空", fontSize = 17.sp, color = IosRed)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
