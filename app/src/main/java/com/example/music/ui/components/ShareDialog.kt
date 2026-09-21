package com.example.music.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * 分享弹窗：展示分享码，提供"分享到…"（系统分享，可发微信）和"复制"。
 */
@Composable
fun ShareDialog(
    shareText: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scroll = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("分享") },
        text = {
            Column(Modifier.verticalScroll(scroll)) {
                Text("把下面的分享码发给好友，好友在「设置 → 导入分享码」里粘贴即可。")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = shareText,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    label = { Text("分享码") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                val chooser = Intent.createChooser(sendIntent, "分享到")
                context.startActivity(chooser)
                onDismiss()
            }) { Text("分享到…") }
        },
        dismissButton = {
            TextButton(onClick = {
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("分享码", shareText))
                Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
                onDismiss()
            }) { Text("复制") }
        }
    )
}