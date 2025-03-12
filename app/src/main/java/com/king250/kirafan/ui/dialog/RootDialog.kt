package com.king250.kirafan.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import com.king250.kirafan.Env
import com.king250.kirafan.activity.MainActivity

@Composable
fun RootDialog(a: MainActivity) {
    val isOpen by a.v.openRoot.collectAsState()

    if (isOpen) {
        AlertDialog(
            onDismissRequest = {
                a.v.setIsRoot(false)
            },
            title = {
                Text("提示")
            },
            text = {
                Text(
                    text = "好像你的设备已经开启了Root且未正确隐藏，这会使得游戏检测到异常并自动闪退，请问要退出进行调整还是继续游玩？",
                    lineHeight = 24.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        a.v.setIsRoot(false)
                    }
                ) {
                    Text("退出")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val intent = a.packageManager.getLaunchIntentForPackage(Env.TARGET_PACKAGE)
                        a.startActivity(intent)
                        a.v.setIsRoot(false)
                    }
                ) {
                    Text("继续游玩")
                }
            }
        )
    }
}
