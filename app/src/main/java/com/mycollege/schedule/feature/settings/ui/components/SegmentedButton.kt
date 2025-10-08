package com.mycollege.schedule.feature.settings.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.shared.ui.theme.buttons
import com.mycollege.schedule.shared.ui.theme.disabledBlue
import com.mycollege.schedule.shared.ui.theme.disabledWhite

@Composable
fun SegmentedButton(checkedState: Boolean, enabled: Boolean = true, onChanged: (Boolean) -> Unit) {
    var selectedIndex by remember { mutableIntStateOf(if (!checkedState) 0 else 1) }
    val options = listOf("Неделя 1", "Неделя 2")

    LaunchedEffect(checkedState) {
        selectedIndex = if (!checkedState) 0 else 1
    }

    Surface(
        modifier = Modifier.wrapContentSize()
            .padding(horizontal = 20.dp),
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 2.dp
    ) {

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {

            options.forEachIndexed { index, option ->

                val isSelected = selectedIndex == index
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) buttons else Color.White,
                    animationSpec = tween(durationMillis = 300), label = ""
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color.Black,
                    animationSpec = tween(durationMillis = 300), label = ""
                )

                SegmentedButton(
                    selected = isSelected,
                    modifier = Modifier
                        .weight(1f),
                    onClick = {
                        selectedIndex = index
                        if (!isSelected) onChanged(!checkedState)
                    },
                    enabled = enabled,
                    shape = if (index == 0)
                        RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
                    else RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = backgroundColor,
                        activeBorderColor = Color.Transparent,
                        inactiveBorderColor = Color.Transparent,
                        inactiveContainerColor = Color.White,
                        activeContentColor = contentColor,
                        inactiveContentColor = contentColor,
                        disabledActiveContainerColor = disabledBlue,
                        disabledInactiveContainerColor = disabledWhite,
                        disabledInactiveBorderColor = Color.Transparent,
                        disabledActiveContentColor = Color.White,
                        disabledInactiveContentColor = Color.LightGray
                    ),
                    icon = {}
                ) {
                    Text(text = option, fontSize = 17.sp, fontWeight = FontWeight.Normal)
                }
            }
        }

    }

}

