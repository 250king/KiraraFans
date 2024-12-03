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
fun VersionBadDialog(a: MainActivity) {
    val isOpen by a.v.isVersionBad.collectAsState()
    val version by a.v.version.collectAsState()

    if (isOpen) {
        AlertDialog(
            onDismissRequest = {
                a.v.setIsVersionBad(false)
            },
            title = {
                Text("提示")
            },
            text = {
                if (version == "3.7.0") {
                    val text = """
                        你好像使用的是骨灰盒版本，这个版本是无法正常游戏的，只能删掉并安装正确的版本。
                        由于这是你最珍贵的存档之一，我们非常不推荐以抛弃骨灰盒的代价来玩，建议换别的设备继续玩吧！
                        如果你已知悉相关风险或者确定骨灰盒没有任何内容，请自行将骨灰盒删除后再试。
                    """.trimIndent()
                    Text(text, lineHeight = 24.sp)
                }
                else {
                    Text("只有3.6.0才能正常使用哟！是否要下载并安装对应的版本？", lineHeight = 24.sp)
                }
            },
            confirmButton = {
                if (version != "3.7.0") {
                    TextButton(
                        onClick = {
                            a.v.setIsVersionBad(false)
                            a.install(a.app)
                        }
                    ) {
                        Text("确定")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        a.v.setIsVersionBad(false)
                    }
                ) {
                    if (version == "3.7.0") {
                        Text("关闭")
                    }
                    else {
                        Text("取消")
                    }
                }
            }
        )
    }
}
