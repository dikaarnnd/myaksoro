package com.dika.myaksoro.ui.screens

// --- Android Framework & Graphics ---
import android.content.Context
import android.graphics.Bitmap

// --- Jetpack Compose: Layout & Foundation ---
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

// --- Jetpack Compose: Material 3 (Komponen UI Bawaan) ---
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text

// --- Jetpack Compose: Runtime & State Management ---
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue

// --- Jetpack Compose: UI, Modifier, Font & Units ---
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Kotlin Coroutines (Async Tasks) ---
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// --- Proyek Internal: Engine & Data Layer ---
import com.dika.myaksoro.AksoroEngine
import com.dika.myaksoro.data.HistoryItem
import com.dika.myaksoro.data.HistoryManager

// --- Proyek Internal: Custom UI Components (Komponen Buatan Sendiri) ---
import com.dika.myaksoro.ui.components.CaptureCard
import com.dika.myaksoro.ui.components.ImageSourceButtons
import com.dika.myaksoro.ui.components.ProcessingOverlay
import com.dika.myaksoro.ui.components.RecentHistorySection
import com.dika.myaksoro.ui.components.ResultCard

// --- Proyek Internal: Theme & Styling ---
import com.dika.myaksoro.ui.theme.AksoroColors

@Composable
fun HomeScreen(
    engine: AksoroEngine,
    context: Context,
    colors: AksoroColors,
    historyList: MutableList<HistoryItem>,
    selectedBitmap: Bitmap?,
    onSelectedBitmapChange: (Bitmap?) -> Unit,
    cnnResult: List<String>,
    onCnnResultChange: (List<String>) -> Unit,
    chunkResult: List<String>,
    onChunkResultChange: (List<String>) -> Unit,
    lstmResult: String,
    onLstmResultChange: (String) -> Unit,
    showProcessButton: Boolean,
    onShowProcessButtonChange: (Boolean) -> Unit,
    isInferencing: Boolean,
    onInferencingChange: (Boolean) -> Unit,
    appFont: FontFamily
) {
    val coroutineScope = rememberCoroutineScope()

    var loadingProgress by remember { mutableStateOf(0f) }
    var loadingText by remember { mutableStateOf("") }

    fun runTransliteration() {
        val bitmap = selectedBitmap ?: return
        onInferencingChange(true)
        onShowProcessButtonChange(false)

        coroutineScope.launch(Dispatchers.Main) {
            loadingProgress = 0f
            loadingText = "Memulai..."

            val (engineResult, savedPath) = withContext(Dispatchers.IO) {
                val res = engine.processImage(bitmap) { progress, text ->
                    loadingProgress = progress
                    loadingText = text
                }
                val path = HistoryManager.saveBitmapToInternalStorage(context, res.debugImage ?: bitmap)
                Pair(res, path)
            }

            loadingProgress = 1.0f
            loadingText = "Selesai!"
            delay(400)

            val debugImg = engineResult.debugImage ?: bitmap
            onSelectedBitmapChange(debugImg)
            onCnnResultChange(engineResult.cnnOutput)
            onChunkResultChange(engineResult.chunkResults)
            onLstmResultChange(engineResult.lstmOutput)

            val newItem = HistoryItem(
                imagePath = savedPath,
                cnnOutput = engineResult.cnnOutput,
                chunkResults = engineResult.chunkResults,
                lstmOutput = engineResult.lstmOutput,
                bitmapCache = debugImg
            )
            historyList.add(0, newItem)
            HistoryManager.saveHistory(context, historyList)

            onInferencingChange(false)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgApp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CaptureCard(bitmap = selectedBitmap, colors = colors, appFont = appFont)

            Spacer(modifier = Modifier.height(24.dp))

            ImageSourceButtons(
                context = context,
                colors = colors,
                appFont = appFont,
                onImagePicked = { bitmap ->
                    onSelectedBitmapChange(bitmap)
                    onCnnResultChange(emptyList())
                    onChunkResultChange(emptyList())
                    onLstmResultChange("...")
                    onShowProcessButtonChange(true)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            ResultCard(
                cnnResult = cnnResult,
                chunkResult = chunkResult,
                lstmResult = lstmResult,
                colors = colors,
                appFont = appFont
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (showProcessButton) {
                Button(
                    onClick = { runTransliteration() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.btnAccent),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        "Mulai Transliterasi",
                        fontFamily = appFont,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = colors.textOnPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            RecentHistorySection(
                items = historyList.take(3),
                colors = colors,
                appFont = appFont
            )
        }

        if (isInferencing) {
            ProcessingOverlay(
                progress = loadingProgress,
                text = loadingText,
                colors = colors,
                appFont = appFont
            )
        }
    }
}