package com.mycollege.schedule.feature.settings.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.shared.ui.theme.backgroundDark
import com.mycollege.schedule.shared.ui.theme.buttons
import com.mycollege.schedule.shared.ui.theme.secondaryDark
import com.mycollege.schedule.shared.ui.theme.tertiaryDark

@Composable
fun CardSettings(painter: Painter, title: String, checkedState: Boolean, onChanged: (Boolean) -> Unit) {

    val darkMode = isSystemInDarkTheme()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .size(width = 0.dp, height = 65.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 5.dp, end = 15.dp)
                .padding(end = 10.dp),
            horizontalArrangement = Arrangement.Absolute.Left,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                painter,
                "icon",
                tint = buttons,
                modifier = Modifier.size(27.dp)
            )
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                text = title,
                color = if (darkMode) Color.White else Color.Black,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = checkedState,
                onCheckedChange = onChanged,
                modifier = Modifier.padding(10.dp).size(15.dp),
                colors = SwitchDefaults.colors(
                    checkedTrackColor = buttons,
                    uncheckedTrackColor = if (darkMode) tertiaryDark else Color.LightGray,
                    uncheckedBorderColor = if (darkMode) backgroundDark else Color.Gray,
                    checkedBorderColor = buttons,
                    checkedThumbColor = if (darkMode) backgroundDark else Color.White,
                    uncheckedThumbColor = if (darkMode) backgroundDark else Color.White,
                )
            )
        }
    }
}

@Composable
fun CardSettings(painter: ImageVector, title: String, checkedState: Boolean, onChanged: (Boolean) -> Unit) {

    val darkMode = isSystemInDarkTheme()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .size(width = 0.dp, height = 65.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 5.dp, end = 15.dp)
                .padding(end = 10.dp),
            horizontalArrangement = Arrangement.Absolute.Left,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                painter,
                "icon",
                tint = buttons,
                modifier = Modifier.size(27.dp)
            )
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                text = title,
                color = if (darkMode) Color.White else Color.Black,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = checkedState,
                onCheckedChange = onChanged,
                modifier = Modifier.padding(10.dp).size(15.dp),
                colors = SwitchDefaults.colors(
                    checkedTrackColor = buttons,
                    uncheckedTrackColor = if (darkMode) tertiaryDark else Color.LightGray,
                    uncheckedBorderColor = if (darkMode) backgroundDark else Color.Gray,
                    checkedBorderColor = buttons,
                    checkedThumbColor = if (darkMode) backgroundDark else Color.White,
                    uncheckedThumbColor = if (darkMode) backgroundDark else Color.White,
                )
            )
        }
    }
}