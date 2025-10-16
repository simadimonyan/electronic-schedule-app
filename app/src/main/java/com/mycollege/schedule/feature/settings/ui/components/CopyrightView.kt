package com.mycollege.schedule.feature.settings.ui.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.BackEventCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("ContextCastToActivity")
@Composable
fun CopyrightView(onDisposable: () -> Unit) {

    val scope = rememberCoroutineScope()

    var visible by remember { mutableStateOf(false) }
    var showTopBar by remember { mutableStateOf(true) }

    val view = LocalView.current
    val activity = LocalContext.current as ComponentActivity

    LaunchedEffect(Unit) {
        WindowInsetsControllerCompat(activity.window, view).isAppearanceLightStatusBars = false
    }

    PredictiveBackHandler(true) { progress: Flow<BackEventCompat> ->
        try {
            progress.collect { backEvent -> }
            scope.launch {
                visible = !visible
                delay(250L)
                WindowInsetsControllerCompat(activity.window, view).isAppearanceLightStatusBars = true
                onDisposable()
            }
        } catch (e: CancellationException) {
            Log.w("PostCreateSwipe", e)
        }
    }

    LaunchedEffect(Unit) {
        visible = true
    }

    val targetAlpha = remember(visible) { if (visible) 0.8f else 0f }
    val animatedAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 300)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = animatedAlpha))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        showTopBar = !showTopBar
                    }
                )
            }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            color = Color.Transparent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Box {

                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(
                            animationSpec = tween(durationMillis = 300)
                        ) + slideInVertically(
                            animationSpec = tween(durationMillis = 300),
                            initialOffsetY = { it }
                        ),
                        exit = fadeOut(
                            animationSpec = tween(durationMillis = 300)
                        ) + slideOutVertically(
                            animationSpec = tween(durationMillis = 300),
                            targetOffsetY = { it }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .align(Alignment.BottomCenter)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        ) {
                            PdfViewerFromAssets("copyrights.pdf")
                        }
                    }
                }

                AnimatedVisibility(
                    visible = visible && showTopBar,
                    enter = fadeIn(
                        animationSpec = tween(durationMillis = 300)
                    ) + slideInVertically(
                        animationSpec = tween(durationMillis = 300),
                        initialOffsetY = { -it }
                    ),
                    exit = fadeOut(
                        animationSpec = tween(durationMillis = 300)
                    ) + slideOutVertically(
                        animationSpec = tween(durationMillis = 300),
                        targetOffsetY = { -it }
                    ),
                ) {
                    TopAppBar(
                        title = {
                            Text("Интеллектуальная собственность", fontSize = 20.sp, color = Color.White)
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    visible = !visible
                                    delay(250L)
                                    WindowInsetsControllerCompat(activity.window, view).isAppearanceLightStatusBars = true
                                    onDisposable()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Назад",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Black.copy(alpha = 0.3f),
                            scrolledContainerColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }

}