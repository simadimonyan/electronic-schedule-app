package com.mycollege.schedule.feature.schedule.ui.components.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun Loader(resource: Int, height: Dp) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resource))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    val isAnimationReady = composition != null

    Column(
        modifier = Modifier
            .height(height)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isAnimationReady) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 20.dp)
            )
        } else {
            Text(text = "Загрузка анимации...", color = Color.Gray)
        }
    }
}