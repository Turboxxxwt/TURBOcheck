package com.student.qujian.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.student.qujian.BuildConfig
import com.student.qujian.R
import kotlinx.coroutines.launch

private const val DOUYIN_URL = "https://v.douyin.com/_I6YwkdV5zw/"
private const val FEEDBACK_EMAIL = "turboxwt@outlook.com"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
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
                    "关于APP",
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
            Spacer(Modifier.height(24.dp))

            // 品牌区：图标 + 名称 + 版本
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.mipmap.ic_launcher),
                    contentDescription = "TURBO查件图标",
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "TURBO查件",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "版本 ${BuildConfig.VERSION_NAME}",
                    fontSize = 14.sp,
                    color = Subtle
                )
            }

            Spacer(Modifier.height(32.dp))

            IosSectionHeader("作者")
            IosCard {
                IosRow(title = "APP作者:Turboxxxwt")
            }

            Spacer(Modifier.height(24.dp))

            IosSectionHeader("联系")
            IosCard {
                IosRow(
                    title = "作者抖音主页",
                    trailing = { IosChevron() },
                    onClick = {
                        openUrl(context, DOUYIN_URL, "无法打开抖音主页")
                    }
                )
                IosDivider()
                IosRow(
                    title = "APP问题反馈邮箱",
                    trailing = { IosChevron() },
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$FEEDBACK_EMAIL")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(FEEDBACK_EMAIL))
                            putExtra(Intent.EXTRA_SUBJECT, "TURBO查件 问题反馈")
                        }
                        try {
                            context.startActivity(
                                Intent.createChooser(intent, "发送反馈邮件")
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        } catch (_: ActivityNotFoundException) {
                            scope.launch { snackbar.showSnackbar("未安装邮件应用") }
                        }
                    }
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

private fun openUrl(context: android.content.Context, url: String, error: String) {
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: ActivityNotFoundException) {
        android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
    }
}
