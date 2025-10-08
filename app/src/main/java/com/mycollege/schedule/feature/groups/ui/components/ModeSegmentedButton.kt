package com.mycollege.schedule.feature.groups.ui.components

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

@Composable
fun ModeSegmentedButton(studentMode: Boolean, onChanged: (Boolean) -> Unit) {

    var selectedIndex by remember { mutableIntStateOf(if (studentMode) 0 else 1) }
    val modes = listOf("Студент", "Преподаватель")

    Surface(
        modifier = Modifier.wrapContentSize()
            .padding(horizontal = 30.dp),
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 2.dp
    ) {

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {

            modes.forEachIndexed { index, option ->

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
                        onChanged(index == 0)
                    },
                    shape = if (index == 0)
                        RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
                    else RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = backgroundColor,
                        activeBorderColor = Color.Transparent,
                        inactiveBorderColor = Color.Transparent,
                        inactiveContainerColor = Color.White,
                        activeContentColor = contentColor
                    ),
                    icon = {}
                ) {
                    Text(text = option, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = contentColor)
                }
            }
        }

    }
}

