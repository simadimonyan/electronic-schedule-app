package com.mycollege.schedule.feature.schedule.ui.components.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mycollege.schedule.shared.ui.theme.LocalAppDarkTheme
import com.mycollege.schedule.shared.ui.theme.buttons
import com.mycollege.schedule.shared.ui.theme.secondaryDark

@Composable
fun SettingsButton(navigateToSettings: () -> Unit) {

    val darkMode = LocalAppDarkTheme.current

    Card(
        colors = CardDefaults.cardColors(containerColor = if (darkMode) secondaryDark else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        IconButton(
            onClick = navigateToSettings,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "settings",
                tint = buttons
            )
        }
    }
}