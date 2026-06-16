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
    manropeFont: FontFamily
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
                    Image(
                        bitmap = selectedBitmap!!.asImageBitmap(),
                        contentDescription = "Preview Gambar",
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        contentScale = ContentScale.FillWidth
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada gambar", fontFamily = manropeFont, color = colors.textTertiary)
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
                    // TAMPILAN SAAT KOSONG (TEPAT DI TENGAH)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp), // Padding atas-bawah yang lebih besar agar proporsional
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Hasil transliterasi akan muncul di sini.",
                            fontFamily = manropeFont,
                            fontSize = 14.sp,
                            color = colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    // TAMPILAN SETELAH AI SELESAI MEMPROSES (RATA KIRI / START)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("HASIL KLASIFIKASI", fontFamily = manropeFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                        Text(cnnResult.toString(), fontFamily = manropeFont, fontSize = 14.sp, color = colors.textPrimary, modifier = Modifier.padding(top = 4.dp))

                        Spacer(modifier = Modifier.height(16.dp))

                        if (chunkResult.isNotEmpty()) {
                            Text("CARA MEMBACA:", fontFamily = manropeFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            chunkResult.forEachIndexed { index, chunkStr ->
                                Text("${index + 1}. $chunkStr", fontFamily = manropeFont, fontSize = 14.sp, color = colors.textPrimary)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Text("HASIL TRANSLASI", fontFamily = manropeFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                        Text(lstmResult, fontFamily = manropeFont, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.padding(top = 8.dp))
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
                    Text("Ambil Foto", fontFamily = manropeFont, fontWeight = FontWeight.Bold, color = colors.textOnPrimary)
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
                    Text("Unggah Foto", fontFamily = manropeFont, fontWeight = FontWeight.Bold, color = colors.textOnSecondary)
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

                            // Jalankan proses mesin AI di background
                            val (engineResult, savedPath) = withContext(Dispatchers.IO) {
                                // TANGKAP LAPORAN PROGRESS DARI MESIN SECARA REAL-TIME
                                val res = engine.processImage(selectedBitmap!!) { progress, text ->
                                    loadingProgress = progress
                                    loadingText = text
                                }
                                val path = HistoryManager.saveBitmapToInternalStorage(context, res.debugImage ?: selectedBitmap!!)
                                Pair(res, path)
                            }

                            // AI selesai bekerja! Penuhkan progress bar
                            loadingProgress = 1.0f
                            loadingText = "Selesai!"
                            delay(400) // Tahan sebentar agar user sempat membaca tulisan "Selesai!"

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
                    Text("Mulai Transliterasi AI", fontFamily = manropeFont, fontWeight = FontWeight.Bold, color = colors.textOnPrimary)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val recentHistory = historyList.take(3)
            if (recentHistory.isNotEmpty()) {
                Divider(color = colors.textTertiary, thickness = 1.dp, modifier = Modifier.padding(bottom = 16.dp).background(Color.Transparent))

                Text(
                    text = "Riwayat Terbaru",
                    fontFamily = manropeFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
                )

                recentHistory.forEachIndexed { index, item ->
                    val cardColor = if (index % 2 == 0) colors.boxHistoryPrimary else colors.boxHistorySecondary

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            if (item.bitmapCache != null) {
                                Image(
                                    bitmap = item.bitmapCache!!.asImageBitmap(),
                                    contentDescription = "Gambar Riwayat",
                                    modifier = Modifier.fillMaxWidth().wrapContentHeight().heightIn(max = 450.dp),
                                    contentScale = ContentScale.FillWidth
                                )
                            }
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Klasifikasi: ${if (item.cnnOutput.isEmpty()) "[]" else item.cnnOutput.toString()}", fontFamily = manropeFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                                Spacer(modifier = Modifier.height(8.dp))

                                if (item.chunkResults.isNotEmpty()) {
                                    Text("Cara membaca:", fontFamily = manropeFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                                    item.chunkResults.forEachIndexed { idx, chunk ->
                                        Text("${idx + 1}. $chunk", fontFamily = manropeFont, fontSize = 14.sp, color = colors.textPrimary)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Text("Hasil: ${item.lstmOutput}", fontFamily = manropeFont, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
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
                            fontFamily = manropeFont,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = loadingText,
                        fontFamily = manropeFont,
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}