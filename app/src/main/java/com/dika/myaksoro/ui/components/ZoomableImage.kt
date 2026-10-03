package com.dika.myaksoro.ui.components

// --- Jetpack Compose: Foundation & Gestures ---
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape

// --- Jetpack Compose: Material 3 ---
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn

// --- Jetpack Compose: Runtime & State Management ---
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// --- Jetpack Compose: UI, Graphics & Layout ---
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Proyek Internal: Theme ---
import com.dika.myaksoro.ui.theme.AksoroColors

@Composable
fun ZoomableImage(
    bitmap: ImageBitmap,
    contentDescription: String,
    appFont: FontFamily,
    colors: AksoroColors,
    isHistoryCard: Boolean = false
) {
    var boxSize by remember { mutableStateOf(Size.Zero) }
    var scale by remember { mutableStateOf(0f) }
    var minScale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val boxModifier = if (isHistoryCard) {
        Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .onSizeChanged { boxSize = Size(it.width.toFloat(), it.height.toFloat()) }
    } else {
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .heightIn(min = 180.dp, max = 450.dp)
            .clip(RoundedCornerShape(16.dp))
            .onSizeChanged { boxSize = Size(it.width.toFloat(), it.height.toFloat()) }
    }

    Box(
        modifier = boxModifier
            .pointerInput(boxSize) {
                if (boxSize == Size.Zero) return@pointerInput

                awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown()
                        do {
                            val event = awaitPointerEvent()
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()

                            val isZooming = event.changes.size > 1
                            val isZoomed = scale > minScale

                            if (isZooming || isZoomed) {
                                event.changes.forEach { it.consume() }

                                val imgW = bitmap.width.toFloat()
                                val imgH = bitmap.height.toFloat()
                                val fitScale = minOf(boxSize.width / imgW, boxSize.height / imgH)

                                scale = (scale * zoom).coerceIn(minScale, minScale * 5f)

                                if (scale < minScale + 0.01f) {
                                    scale = minScale
                                    offset = Offset.Zero
                                } else {
                                    val dW = imgW * fitScale
                                    val dH = imgH * fitScale

                                    val maxX = maxOf(0f, (dW * scale - boxSize.width) / 2f)
                                    val maxY = maxOf(0f, (dH * scale - boxSize.height) / 2f)

                                    val newX = (offset.x + pan.x).coerceIn(-maxX, maxX)
                                    val newY = (offset.y + pan.y).coerceIn(-maxY, maxY)
                                    offset = Offset(newX, newY)
                                }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
            }
    ) {
        if (boxSize != Size.Zero) {
            val imgW = bitmap.width.toFloat()
            val imgH = bitmap.height.toFloat()
            val fitScale = minOf(boxSize.width / imgW, boxSize.height / imgH)

            if (scale == 0f) {
                scale = 1f
                minScale = 1f
            }

            val dW = imgW * fitScale
            val dH = imgH * fitScale
            val maxX = maxOf(0f, (dW * scale - boxSize.width) / 2f)
            val maxY = maxOf(0f, (dH * scale - boxSize.height) / 2f)

            offset = Offset(
                offset.x.coerceIn(-maxX, maxX),
                offset.y.coerceIn(-maxY, maxY)
            )

            Image(
                bitmap = bitmap,
                contentDescription = contentDescription,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentScale = ContentScale.Fit
            )

            if (scale <= minScale * 1.05f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ZoomIn,
                        contentDescription = "Zoom In Indicator",
                        tint = colors.btnAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}