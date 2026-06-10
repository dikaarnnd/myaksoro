package com.dika.myaksoro.ui.screens

import com.dika.myaksoro.data.HistoryItem
import com.dika.myaksoro.data.HistoryManager
import com.dika.myaksoro.ui.theme.AksoroColors

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@Composable
fun HistoryScreen(
    context: Context,
    historyList: MutableList<HistoryItem>,
    colors: AksoroColors,
    manropeFont: FontFamily
) {
    var isAscending by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val displayedList = if (isAscending) historyList.reversed() else historyList.toList()

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text("Hapus Semua Riwayat?", fontFamily = manropeFont, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            },
            text = {
                Text("Tindakan ini tidak dapat dibatalkan dan semua gambar akan dihapus permanen dari memori HP-mu.", fontFamily = manropeFont, color = colors.textSecondary)
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
                    Text("Hapus", color = Color(0xFFD32F2F), fontFamily = manropeFont, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal", color = colors.textPrimary, fontFamily = manropeFont)
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
                        .padding(top = 24.dp, bottom = 8.dp),
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
                            text = if (isAscending) "Waktu: Terlama" else "Waktu: Terbaru",
                            fontFamily = manropeFont,
                            fontWeight = FontWeight.Bold,
                            color = colors.textOnSecondary,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            // .background(Color(0x1AD32F2F), RoundedCornerShape(12.dp))
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
                        Text("Belum ada riwayat translasi.", fontFamily = manropeFont, color = colors.textTertiary)
                    }
                } else {
                    displayedList.forEachIndexed { index, item ->
                        val cardColor = if (index % 2 == 0) colors.boxHistoryPrimary else colors.boxHistorySecondary

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
        }
    }
}