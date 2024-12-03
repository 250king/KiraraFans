package com.king250.kirafan.ui.component.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import com.king250.kirafan.ui.activity.MainActivity

@Composable
fun UnsupportedDialog(a: MainActivity) {
    val isOpen by a.v.isUnsupported.collectAsState()

    if (isOpen) {
        AlertDialog(
            onDismissRequest = {
                a.v.setIsUnsupported(false)
            },
            title = {
                Text("提示")
            },
            text = {
                Text(
                    text = "因为游戏不允许Android 14+的设备运行。只能通过安装VMOS并在内部安装好游戏后继续游玩。请问是否要继续下载VMOS并安装？",
                    lineHeight = 24.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        a.v.setIsUnsupported(false)
                        a.install(a.app)
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        a.v.setIsUnsupported(false)
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
}
