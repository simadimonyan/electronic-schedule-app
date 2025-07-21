package com.mycollege.schedule.feature.settings.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.shared.ui.theme.buttons

@Composable
fun CardSettings(title: String, checkedState: Boolean, onChanged: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .size(width = 0.dp, height = 65.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = checkedState,
                onCheckedChange = onChanged,
                modifier = Modifier.padding(10.dp),
                colors = SwitchDefaults.colors(
                    checkedTrackColor = buttons,
                    uncheckedTrackColor = Color.LightGray,
                    uncheckedBorderColor = Color.Gray,
                    checkedBorderColor = buttons,
                    checkedThumbColor = Color.White,
                    uncheckedThumbColor = Color.White,
                )
            )
        }
    }
}