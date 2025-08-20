package com.mycollege.schedule.shared.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay


@Preview
@Composable
fun ModeTransitionPreview() {
    ModeTransition(true)
}

@Composable
fun ModeTransition(studentMode: Boolean) {

    var visibleBackground by remember { mutableStateOf(false) }
    var visibleModeText by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visibleBackground = true
        delay(300)
        visibleModeText = true
        delay(1000)
        visibleBackground = false
    }

    AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = visibleBackground,
        enter = fadeIn(animationSpec = tween(700)),
        exit = fadeOut(animationSpec = tween(700))
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.White),
            contentAlignment = Alignment.CenterStart
        ) {
            AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                visible = visibleModeText,
                enter = slideInVertically(
                    initialOffsetY = { it }
                ) + fadeIn(animationSpec = tween(700)),
                exit = slideOutVertically(
                    targetOffsetY = { it }
                ) + fadeOut(animationSpec = tween(700))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Column(Modifier.padding(bottom = 40.dp), horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "РЕЖИМ",
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            fontSize = 20.sp,
                        )
                        Text(
                            text = if (studentMode) "СТУДЕНТА" else "ПРЕПОДАВАТЕЛЯ",
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            fontSize = 38.sp
                        )
                    }
                }
            }
        }
    }

}