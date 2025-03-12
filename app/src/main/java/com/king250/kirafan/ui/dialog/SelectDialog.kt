package com.king250.kirafan.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
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
    val endpoints by a.v.endpoints.collectAsState()
    val selectedEndpoint by a.v.selectedEndpoint.collectAsState()
    val isOpen by a.v.openSelect.collectAsState()

    if (isOpen) {
        AlertDialog(
            onDismissRequest = {
                a.v.setOpenSelect(false)
            },
            title = {
                Text("选择节点")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        a.v.setIsRoot(false)
                    }
                ) {
                    Text("取消")
                }
            },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    endpoints.forEachIndexed { index, it ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .selectable(
                                onClick = {
                                    a.change(index)
                                    a.v.setSelectedEndpoint(index)
                                    a.v.setOpenSelect(false)
                                },
                                selected = index == selectedEndpoint
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                onClick = {
                                    a.change(index)
                                    a.v.setSelectedEndpoint(index)
                                    a.v.setOpenSelect(false)
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
