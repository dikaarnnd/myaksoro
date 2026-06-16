package com.dika.myaksoro.ui.screens

import com.dika.myaksoro.data.HistoryItem
import com.dika.myaksoro.data.HistoryManager
import com.dika.myaksoro.ui.theme.AksoroColors

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import java.io.File

// IMPORT TAMBAHAN UNTUK FITUR ZOOM
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip

@Composable
fun HistoryScreen(
    context: Context,
    historyList: MutableList<HistoryItem>,
    colors: AksoroColors,
    appFont: FontFamily
) {
    var isAscending by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val displayedList = if (isAscending) historyList.reversed() else historyList.toList()

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text("Hapus Semua Riwayat?", fontFamily = appFont, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
            },
            text = {
                Text("Tindakan ini tidak dapat dibatalkan dan semua gambar akan dihapus permanen dari memori HP-mu.", fontFamily = appFont, color = colors.textSecondary)
            },
            confirmButton = {
                TextButton(onClick = {
                    historyList.forEach {
                        val file = File(it.imagePath)
                        if (file.exists()) file.delete()
                    }
                    historyList.clear()
                    HistoryManager.saveHistory(context, historyList)
                    showDeleteDialog = false
                }) {
                    Text("Hapus", color = Color(0xFFD32F2F), fontFamily = appFont, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal", color = colors.textPrimary, fontFamily = appFont, fontWeight = FontWeight.Medium)
                }
            },
            containerColor = colors.cardBg
        )
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
        ) {
            if (historyList.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { isAscending = !isAscending },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.btnSecondary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = if (isAscending) "Waktu: Terdahulu" else "Waktu: Terbaru",
                            fontFamily = appFont,
                            fontWeight = FontWeight.Bold,
                            color = colors.textOnSecondary,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Hapus Semua",
                            tint = Color(0xFFD32F2F)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (historyList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(top = 64.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada riwayat transliterasi.", fontFamily = appFont, fontWeight = FontWeight.Medium, color = colors.textTertiary)
                    }
                } else {
                    displayedList.forEachIndexed { index, item ->
                        // val cardColor = if (index % 2 == 0) colors.boxHistoryPrimary else colors.boxHistorySecondary
                        val cardColor = colors.boxHistorySecondary

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
