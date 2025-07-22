package com.mycollege.schedule.feature.settings.ui.components

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.TransformOrigin
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun PdfViewerFromAssets(assetFileName: String) {
    val context = LocalContext.current
    val imageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }

    var scale by remember { mutableStateOf(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 10f)
        coroutineScope.launch {
            offsetX.snapTo(offsetX.value + offsetChange.x)
            offsetY.snapTo(offsetY.value + offsetChange.y)
        }
    }

    LaunchedEffect(assetFileName) {
        val file = File(context.cacheDir, assetFileName)
        if (!file.exists()) {
            context.assets.open(assetFileName).use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }

        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(fileDescriptor)
        if (pdfRenderer.pageCount > 0) {
            val page = pdfRenderer.openPage(0)
            val bitmap = createBitmap(page.width * 3, page.height * 3)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            pdfRenderer.close()
            fileDescriptor.close()
            imageBitmap.value = bitmap.asImageBitmap()
        }
    }
    imageBitmap.value?.let { bitmap ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                    }
                }
                .transformable(state = state)
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = "copyrights",
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX.value,
                        translationY = offsetY.value,
                        transformOrigin = TransformOrigin.Center
                    )
            )

            LaunchedEffect(state.isTransformInProgress) {
                if (!state.isTransformInProgress) {
                    coroutineScope.launch {
                        offsetX.animateTo(0f, animationSpec = spring(stiffness = 1000f))
                        offsetY.animateTo(0f, animationSpec = spring(stiffness = 1000f))
                    }
                }
            }

        }
    }
}