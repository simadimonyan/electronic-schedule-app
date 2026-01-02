package com.mycollege.schedule.feature.settings.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.mycollege.schedule.R

@Composable
fun ThemeToggleButton(
    modifier: Modifier,
    onToggle: () -> Unit,
    darkTheme: Boolean
) {

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.switch_mode))

    var progress by remember(darkTheme) {
        mutableFloatStateOf(if (darkTheme) 0.5f else 0f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 700),
        label = "Lottie progress"
    )

    val targetColor = if (darkTheme) Color.White else Color(0xFFFFD600)
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 700)
    )

    val colorProperty = rememberLottieDynamicProperty(
        property = LottieProperty.COLOR,
        value = animatedColor.toArgb(),
        keyPath = arrayOf("**")
    )
    val strokeProperty = rememberLottieDynamicProperty(
        property = LottieProperty.STROKE_COLOR,
        value = animatedColor.toArgb(),
        keyPath = arrayOf("**")
    )

    val dynamicProperties = rememberLottieDynamicProperties(colorProperty, strokeProperty)

    LottieAnimation(
        composition = composition,
        progress = { animatedProgress },
        dynamicProperties = dynamicProperties,
        modifier = modifier.zIndex(1f)
            .size(40.dp)
            .clip(CircleShape)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                val targetProgress = if (darkTheme) 1f else 0.5f
                progress = targetProgress
                onToggle()
            }
    )

}