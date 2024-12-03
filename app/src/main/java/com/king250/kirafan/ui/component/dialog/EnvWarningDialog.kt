package com.king250.kirafan.ui.component.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import com.king250.kirafan.Util
import com.king250.kirafan.ui.activity.MainActivity

@Composable
fun EnvWarningDialog(a: MainActivity) {
    val isOpen by a.v.isEnvWarning.collectAsState()

    if (isOpen) {
        AlertDialog(
            onDismissRequest = {
                a.v.setIsEnvWarning(false)
            },
            title = {
                Text("提示")
            },
            text = {
                var content = "好像你已经使用了科技且未正确隐藏，这会使得游戏检测到异常并自动闪退，请问要退出进行调整还是继续游玩？"
                if (Util.isRooted() || Util.isDebug(a.contentResolver) /*|| Util.isEmulator()*/) {
                    content += "\n"
                }
                if (Util.isRooted()) {
                    content += "\n● 有su等root提权工具"
                }
                if (Util.isDebug(a.contentResolver)) {
                    content += "\n● USB调试未关闭"
                }
                /*
                if (Util.isEmulator()) {
                    content += "\n● 存在libhoudini.so文件"
                }
                */
                Text(
                    text = content,
                    lineHeight = 24.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        a.v.setIsEnvWarning(false)
                    }
                ) {
                    Text("退出")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val intent = a.packageManager.getLaunchIntentForPackage(a.app)
                        a.startActivity(intent)
                        a.v.setIsEnvWarning(false)
                    }
                ) {
                    Text("继续游玩")
                }
            }
        )
    }
}
