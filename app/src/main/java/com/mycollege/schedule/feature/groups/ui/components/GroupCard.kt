package com.mycollege.schedule.feature.groups.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.shared.ui.theme.buttons

@Composable
fun GroupCard(icon: Int, title: String, subtitle: String, onClick: () -> Unit, enabled: Boolean = true, darkMode: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(20.dp, 0.dp, 20.dp, 20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = if (darkMode) Color(0xFF34343A) else Color.White),
        onClick = onClick,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .padding(start = 20.dp, top = 5.dp, bottom = 5.dp)
                .wrapContentHeight()
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                colorFilter = ColorFilter.tint(buttons),
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .wrapContentHeight()
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (darkMode) Color.White else Color(0xFF34343A)
                )
                Text(
                    text = subtitle,
                    color = if (darkMode) Color.LightGray else Color.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
}