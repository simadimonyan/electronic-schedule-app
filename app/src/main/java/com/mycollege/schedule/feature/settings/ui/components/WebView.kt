package com.mycollege.schedule.feature.settings.ui.components
import android.annotation.SuppressLint
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mycollege.schedule.shared.ui.theme.LocalAppDarkTheme
import com.mycollege.schedule.shared.ui.theme.background
import com.mycollege.schedule.shared.ui.theme.backgroundDark
import com.mycollege.schedule.shared.ui.theme.disabledWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("ContextCastToActivity")
@Composable
fun WebViewScreen(label: String, link: String, onDisposable: () -> Unit) {

    val scope = rememberCoroutineScope()
    val darkMode = LocalAppDarkTheme.current

    var visible by remember { mutableStateOf(false) }
    var showTopBar by remember { mutableStateOf(true) }

    PredictiveBackHandler(true) { progress: Flow<BackEventCompat> ->
        try {
            progress.collect { backEvent -> }
            scope.launch {
                visible = !visible
                delay(250L)
                onDisposable()
            }
        } catch (e: CancellationException) {
            Log.w("PostCreateSwipe", e)
        }
    }

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
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
                            WebViewContent(link)
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
                            Text(label, fontSize = 18.sp, color = if (darkMode) Color.White else Color.Black)
                            Column {
                                Spacer(Modifier.height(20.dp))
                                Text(link, fontSize = 15.sp, color = if (darkMode) disabledWhite else Color.Gray)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    visible = !visible
                                    delay(250L)
                                    onDisposable()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Назад",
                                    tint = if (darkMode) Color.White else Color.Black
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = if (darkMode) backgroundDark else background
                        )
                    )
                }
            }
        }
    }

}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContent(link: String) {

    Column {

        Spacer(Modifier.height(90.dp))

        AndroidView(
            factory = { context ->
                return@AndroidView WebView(context).apply {
                    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                    settings.javaScriptEnabled = true
                    webViewClient = WebViewClient()

                    settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.setSupportZoom(false)

                    settings.layoutAlgorithm = android.webkit.WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                    settings.setGeolocationEnabled(false)
                }
            },
            update = { webView ->
                if (webView.url != link) {
                    webView.loadUrl(link)
                }
            },
            modifier = Modifier
                .fillMaxSize()
        )
    }

}