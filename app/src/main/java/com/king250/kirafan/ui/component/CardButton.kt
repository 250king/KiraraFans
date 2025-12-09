package com.king250.kirafan.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@Composable
fun CardButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    title: String,
    description: String? = null,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp).alpha(0.9f),
        shape = RoundedCornerShape(30.dp),
        onClick = onClick
    ) {
        Row(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.inversePrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.align(Alignment.CenterVertically)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                description?.let {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}