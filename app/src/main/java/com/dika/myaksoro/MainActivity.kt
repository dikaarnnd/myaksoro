package com.dika.myaksoro

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Import khusus untuk library Crop
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var aksoroEngine: AksoroEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        aksoroEngine = AksoroEngine(this)
        setContent {
            AksoroApp(aksoroEngine)
        }
    }
}

@Composable
fun AksoroApp(engine: AksoroEngine) { // Sesuaikan nama parameter
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cnnResult by remember { mutableStateOf<List<String>>(emptyList()) }
    var lstmResult by remember { mutableStateOf("Belum ada hasil") }

    // PERUBAHAN UTAMA: Fungsi pembuka Kamera/Galeri sekaligus Crop
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

                selectedBitmap = softwareBitmap

                coroutineScope.launch(Dispatchers.Default) {
                    val engineResult = engine.processImage(softwareBitmap)

                    // Kembalikan hasilnya ke Main Thread untuk memperbarui UI
                    withContext(Dispatchers.Main) {
                        // GANTI pratinjau dengan hasil crop pertama dari OpenCV
                        selectedBitmap = engineResult.debugImage ?: softwareBitmap
                        cnnResult = engineResult.cnnOutput
                        lstmResult = engineResult.lstmOutput
                    }
                }
            }
        } else {
            // Pengguna membatalkan proses crop atau terjadi error
            val exception = result.error
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("AksaGo Scanner", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 20.dp))

        // Tombol dimodifikasi untuk memanggil fungsi Crop
        Button(
            onClick = {
                // Konfigurasi Cropper (Bisa pilih kamera/galeri, ada grid bantuan)
                cropImageLauncher.launch(
                    CropImageContractOptions(
                        uri = null,
                        cropImageOptions = CropImageOptions(
                            imageSourceIncludeGallery = true,
                            imageSourceIncludeCamera = true,
                            guidelines = com.canhub.cropper.CropImageView.Guidelines.ON
                        )
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Ambil & Potong Gambar", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.fillMaxWidth().height(250.dp).background(Color.LightGray, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (selectedBitmap != null) {
                Image(bitmap = selectedBitmap!!.asImageBitmap(), contentDescription = "Hasil", modifier = Modifier.fillMaxSize())
            } else {
                Text("Pratinjau Gambar", color = Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Array MobileNetV2:", fontWeight = FontWeight.Bold)
                Text(if (cnnResult.isEmpty()) "[]" else cnnResult.toString(), color = Color.Yellow, modifier = Modifier.padding(top = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Hasil Akhir (Seq2Seq):", fontWeight = FontWeight.Bold)
                Text(lstmResult, fontSize = 20.sp, color = Color(0xFF2E7D32), modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}