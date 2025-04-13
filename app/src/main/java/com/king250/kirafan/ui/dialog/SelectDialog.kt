package com.king250.kirafan.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.king250.kirafan.activity.MainActivity

@Composable
fun SelectDialog(a: MainActivity) {
    val endpoints by a.s.endpoints.collectAsState()
    val selectedEndpoint by a.s.selectedEndpoint.collectAsState()
    val isOpen by a.d.selector.collectAsState()

    if (isOpen) {
        AlertDialog(
            onDismissRequest = {
                a.d.openSelector(false)
            },
            title = {
                Text("选择节点")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        a.d.openSelector(false)
                    }
                ) {
                    Text("取消")
                }
            },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    endpoints.forEachIndexed { index, it ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                onClick = {
                                    a.change(index)
                                    a.s.setSelectedEndpoint(index)
                                    a.d.openSelector(false)
                                },
                                selected = index == selectedEndpoint
                            )
                            Text(
                                text = it.name,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
        )
    }
}
