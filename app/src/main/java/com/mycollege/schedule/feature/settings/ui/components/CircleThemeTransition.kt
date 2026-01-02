package com.mycollege.schedule.feature.settings.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import com.mycollege.schedule.feature.settings.ui.state.SettingsViewModel
import com.mycollege.schedule.shared.ui.theme.LocalAppDarkTheme
import kotlin.math.hypot

@Composable
fun CircleThemeTransition(
    settingsViewModel: SettingsViewModel,
    content: @Composable () -> Unit
) {
    val revealProgress = remember { Animatable(0f) }
    val appState by settingsViewModel.appStateHolder.appState.collectAsState()
    var previousTheme = remember { mutableStateOf(appState.darkTheme) }

    LaunchedEffect(Unit) {
        settingsViewModel.settingsStateHolder.themeChanged.collect {
            if (it) {
                previousTheme.value = appState.darkTheme
                revealProgress.snapTo(0f)
                revealProgress.animateTo(1f, tween(durationMillis = 1000))
                settingsViewModel.settingsStateHolder.clearThemeChange()
            }
        }
    }

    Box(
        Modifier.fillMaxSize()
    ) {

        if (revealProgress.value < 1f) {
            CompositionLocalProvider(LocalAppDarkTheme provides previousTheme.value) {
                content()
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .drawWithCache {
                    val path = Path()
                    val maxRadius = hypot(size.width, size.height) * 1.5f

                    onDrawWithContent {
                        val radius = revealProgress.value * maxRadius
                        path.reset()
                        path.addOval(
                            Rect(center = Offset(size.width, 0f), radius = radius)
                        )
                        clipPath(path) {
                            this@onDrawWithContent.drawContent()
                        }
                    }
                }
        ) {
            if (revealProgress.value > 0f) {
                CompositionLocalProvider(LocalAppDarkTheme provides appState.darkTheme) {
                    content()
                }
            }
        }

    }
}