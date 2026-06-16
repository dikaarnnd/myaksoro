package com.dika.myaksoro.ui.screens

import com.dika.myaksoro.AksoroEngine
import com.dika.myaksoro.data.HistoryItem
import com.dika.myaksoro.data.HistoryManager
import com.dika.myaksoro.ui.theme.AksoroColors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom

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

    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                val softwareBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }
                } else {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri).copy(Bitmap.Config.ARGB_8888, true)
                }

                onSelectedBitmapChange(softwareBitmap)
                onCnnResultChange(emptyList())
                onChunkResultChange(emptyList())
                onLstmResultChange("...")
                onShowProcessButtonChange(true)
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cropImageLauncher.launch(
                CropImageContractOptions(uri = null, cropImageOptions = CropImageOptions(
                    imageSourceIncludeGallery = false, imageSourceIncludeCamera = true,
                    guidelines = com.canhub.cropper.CropImageView.Guidelines.ON
                ))
            )
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .wrapContentHeight()
                    .heightIn(max = 450.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBg)
            ) {
                if (selectedBitmap != null) {
//                    Image(
//                        bitmap = selectedBitmap!!.asImageBitmap(),
//                        contentDescription = "Preview Gambar",
//                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
//                        contentScale = ContentScale.FillWidth
//                    )
                    ZoomableImage(
                        bitmap = selectedBitmap!!.asImageBitmap(),
                        contentDescription = "Preview Gambar",
                        appFont = appFont,
                        isHistoryCard = false
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada gambar", fontFamily = appFont, fontWeight = FontWeight.Medium, color = colors.textTertiary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.btnSecondary),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                if (cnnResult.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Hasil transliterasi akan muncul di sini.",
                            fontFamily = appFont,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "HASIL DETEKSI",
                            fontFamily = appFont,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textSecondary,
                            letterSpacing = 1.5.sp
                        )

                        if (cnnResult.isEmpty()) {
                            Text("-", fontFamily = appFont, fontSize = 14.sp, color = colors.textPrimary, modifier = Modifier.padding(top = 4.dp))
                        } else {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(cnnResult.size) { index ->
                                    Box(
                                        modifier = Modifier
                                            .background(colors.bgApp, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${index + 1}. ${cnnResult[index]}",
                                            fontFamily = appFont,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.5.sp,
                                            color = colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (chunkResult.isNotEmpty()) {
                            Text(
                                text = "CARA MEMBACA",
                                fontFamily = appFont,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textSecondary,
                                letterSpacing = 1.5.sp
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                chunkResult.forEachIndexed { index, chunkStr ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(colors.bgApp, RoundedCornerShape(8.dp))
                                            .padding(14.dp)
                                    ) {
                                        Text(
                                            text = "${index + 1}.  $chunkStr",
                                            fontFamily = appFont,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = 0.3.sp,
                                            lineHeight = 22.sp,
                                            color = colors.textPrimary
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        Text(
                            text = "HASIL TRANSLITERASI",
                            fontFamily = appFont,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textSecondary,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.btnPrimary, RoundedCornerShape(12.dp))
                                .padding(vertical = 20.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lstmResult,
                                fontFamily = appFont,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = colors.textOnPrimary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            cropImageLauncher.launch(
                                CropImageContractOptions(uri = null, cropImageOptions = CropImageOptions(
                                    imageSourceIncludeGallery = false, imageSourceIncludeCamera = true,
                                    guidelines = com.canhub.cropper.CropImageView.Guidelines.ON
                                ))
                            )
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.btnPrimary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Ambil Foto", fontFamily = appFont, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = colors.textOnPrimary)
                }

                Button(
                    onClick = {
                        cropImageLauncher.launch(
                            CropImageContractOptions(uri = null, cropImageOptions = CropImageOptions(
                                imageSourceIncludeGallery = true, imageSourceIncludeCamera = false,
                                guidelines = com.canhub.cropper.CropImageView.Guidelines.ON
                            ))
                        )
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.btnSecondary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Unggah Foto", fontFamily = appFont, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = colors.textOnSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showProcessButton) {
                Button(
                    onClick = {
                        onInferencingChange(true)
                        onShowProcessButtonChange(false)

                        coroutineScope.launch(Dispatchers.Main) {
                            loadingProgress = 0f
                            loadingText = "Memulai..."

                            val (engineResult, savedPath) = withContext(Dispatchers.IO) {
                                val res = engine.processImage(selectedBitmap!!) { progress, text ->
                                    loadingProgress = progress
                                    loadingText = text
                                }
                                val path = HistoryManager.saveBitmapToInternalStorage(context, res.debugImage ?: selectedBitmap!!)
                                Pair(res, path)
                            }

                            loadingProgress = 1.0f
                            loadingText = "Selesai!"
                            delay(400)

                            val debugImg = engineResult.debugImage ?: selectedBitmap!!
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
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.btnAccent),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Mulai Transliterasi", fontFamily = appFont, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = colors.textOnPrimary)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val recentHistory = historyList.take(3)
            if (recentHistory.isNotEmpty()) {
                Divider(color = colors.textTertiary, thickness = 1.dp, modifier = Modifier.padding(bottom = 16.dp).background(Color.Transparent))

                Text(
                    text = "Riwayat Terbaru",
                    fontFamily = appFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
                )

                recentHistory.forEachIndexed { index, item ->
                    // val cardColor = if (index % 2 == 0) colors.boxHistoryPrimary else colors.boxHistorySecondary
                    val cardColor = colors.boxHistorySecondary

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            if (item.bitmapCache != null) {
                                // MENGGUNAKAN KOMPONEN ZOOMABLE IMAGE
                                ZoomableImage(
                                    bitmap = item.bitmapCache!!.asImageBitmap(),
                                    contentDescription = "Gambar Riwayat",
                                    appFont = appFont,
                                    isHistoryCard = true
                                )
                            }
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "HASIL DETEKSI",
                                    fontFamily = appFont,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.textSecondary,
                                    letterSpacing = 1.2.sp
                                )

                                if (item.cnnOutput.isEmpty()) {
                                    Text("-", fontFamily = appFont, fontSize = 12.sp, color = colors.textPrimary, modifier = Modifier.padding(top = 4.dp))
                                } else {
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items(item.cnnOutput.size) { idx ->
                                            Box(
                                                modifier = Modifier
                                                    .background(colors.bgApp, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "${idx + 1}. ${item.cnnOutput[idx]}",
                                                    fontFamily = appFont,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    letterSpacing = 0.5.sp,
                                                    color = colors.textPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))

                                if (item.chunkResults.isNotEmpty()) {
                                    Text(
                                        text = "CARA MEMBACA",
                                        fontFamily = appFont,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = colors.textSecondary,
                                        letterSpacing = 1.2.sp
                                    )
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        item.chunkResults.forEachIndexed { idx, chunk ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(colors.bgApp, RoundedCornerShape(6.dp))
                                                    .padding(10.dp)
                                            ) {
                                                Text(
                                                    text = "${idx + 1}.  $chunk",
                                                    fontFamily = appFont,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    letterSpacing = 0.3.sp,
                                                    color = colors.textPrimary
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                Text(
                                    text = "HASIL TRANSLITERASI",
                                    fontFamily = appFont,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.textSecondary,
                                    letterSpacing = 1.2.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(colors.btnPrimary, RoundedCornerShape(8.dp))
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.lstmOutput,
                                        fontFamily = appFont,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp,
                                        color = colors.textOnPrimary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (isInferencing) {
            Box(
                modifier = Modifier.fillMaxSize().background(colors.overlayBg),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .size(width = 300.dp, height = 220.dp)
                        .background(Color(0xFF222222), RoundedCornerShape(20.dp))
                        .padding(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = loadingProgress,
                            color = Color.White,
                            trackColor = Color.DarkGray,
                            strokeWidth = 6.dp,
                            modifier = Modifier.size(80.dp)
                        )
                        Text(
                            text = "${(loadingProgress * 100).toInt()}%",
                            fontFamily = appFont,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = loadingText,
                        fontFamily = appFont,
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

// ZOOMABLE IMAGE (VERSI FINAL - PERBAIKAN SCROLL LAYAR)
@Composable
fun ZoomableImage(
    bitmap: androidx.compose.ui.graphics.ImageBitmap,
    contentDescription: String,
    appFont: FontFamily,
    isHistoryCard: Boolean = false
) {
    var boxSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var scale by remember { mutableStateOf(0f) }
    var minScale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    val boxModifier = if (isHistoryCard) {
        Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .onSizeChanged { boxSize = androidx.compose.ui.geometry.Size(it.width.toFloat(), it.height.toFloat()) }
    } else {
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .heightIn(min = 180.dp, max = 450.dp)
            .clip(RoundedCornerShape(16.dp))
            .onSizeChanged { boxSize = androidx.compose.ui.geometry.Size(it.width.toFloat(), it.height.toFloat()) }
    }

    Box(
        modifier = boxModifier
            .pointerInput(boxSize) {
                if (boxSize == androidx.compose.ui.geometry.Size.Zero) return@pointerInput

                // MENGGUNAKAN PENGHITUNG GESTURE MANUAL
                awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown()
                        do {
                            val event = awaitPointerEvent()
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()

                            // Cek apakah user menggunakan 2 jari (mencubit) atau gambar sedang di-zoom
                            val isZooming = event.changes.size > 1
                            val isZoomed = scale > minScale

                            // JIKA SEDANG ZOOM/CUBIT: Tahan layar, mainkan gambar!
                            if (isZooming || isZoomed) {
                                // KONSUMSI EVENT: Memblokir parent agar layar tidak ikut ter-scroll
                                event.changes.forEach { it.consume() }

                                val imgW = bitmap.width.toFloat()
                                val imgH = bitmap.height.toFloat()
                                val fitScale = minOf(boxSize.width / imgW, boxSize.height / imgH)

                                scale = (scale * zoom).coerceIn(minScale, minScale * 5f)

                                // Fitur "Snap": Jika pengguna melakukan zoom out hingga mentok, langsung rapikan
                                if (scale < minScale + 0.01f) {
                                    scale = minScale
                                    offset = androidx.compose.ui.geometry.Offset.Zero
                                } else {
                                    val dW = imgW * fitScale
                                    val dH = imgH * fitScale

                                    val maxX = maxOf(0f, (dW * scale - boxSize.width) / 2f)
                                    val maxY = maxOf(0f, (dH * scale - boxSize.height) / 2f)

                                    val newX = (offset.x + pan.x).coerceIn(-maxX, maxX)
                                    val newY = (offset.y + pan.y).coerceIn(-maxY, maxY)
                                    offset = androidx.compose.ui.geometry.Offset(newX, newY)
                                }
                            }
                            // JIKA TIDAK DI-ZOOM (Skala = 1) dan CUMA 1 JARI:
                            // Biarkan kosong! Event tidak akan dikonsumsi, sehingga layar utama bisa discroll dengan lancar.

                        } while (event.changes.any { it.pressed })
                    }
                }
            }
    ) {
        if (boxSize != androidx.compose.ui.geometry.Size.Zero) {
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

            offset = androidx.compose.ui.geometry.Offset(
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
                        .padding(12.dp)
                        .background(Color(0x99000000), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🔍",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = appFont,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}