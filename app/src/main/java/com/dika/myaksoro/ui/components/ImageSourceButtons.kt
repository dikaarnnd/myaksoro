package com.dika.myaksoro.ui.components

// --- Android Framework & Media ---
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat

// --- Activity Result Contracts (Camera & Gallery) ---
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

// --- Jetpack Compose: Foundation & Layout ---
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape

// --- Jetpack Compose: Material 3 ---
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Icon

// --- Jetpack Compose: Runtime & UI ---
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Third-Party Library: Image Cropper ---
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView

// --- Proyek Internal: Theme ---
import com.dika.myaksoro.ui.theme.AksoroColors

@Composable
fun ImageSourceButtons(
    context: Context,
    colors: AksoroColors,
    appFont: FontFamily,
    onImagePicked: (Bitmap) -> Unit,
    onProcessClick: () -> Unit,
    isProcessEnabled: Boolean,
) {
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
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                        .copy(Bitmap.Config.ARGB_8888, true)
                }
                onImagePicked(softwareBitmap)
            }
        }
    }

    fun launchCamera() {
        cropImageLauncher.launch(
            CropImageContractOptions(
                uri = null,
                cropImageOptions = CropImageOptions(
                    imageSourceIncludeGallery = false,
                    imageSourceIncludeCamera = true,
                    guidelines = CropImageView.Guidelines.ON
                )
            )
        )
    }

    fun launchGallery() {
        cropImageLauncher.launch(
            CropImageContractOptions(
                uri = null,
                cropImageOptions = CropImageOptions(
                    imageSourceIncludeGallery = true,
                    imageSourceIncludeCamera = false,
                    guidelines = CropImageView.Guidelines.ON
                )
            )
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) launchCamera()
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Button Ambil Gambar (Camera)
        Button(
            onClick = {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) launchCamera()
                else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            modifier = Modifier.height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.btnAccent,
                contentColor = colors.cardBg
            ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AddAPhoto,
                contentDescription = "Ambil Gambar"
            )
        }

        // 2. Button Unggah Gambar (Gallery)
        Button(
            onClick = { launchGallery() },
            modifier = Modifier.height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.cardBg,
                contentColor = colors.btnAccent
            ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(0.dp),
            border = BorderStroke(1.dp, colors.btnAccent)
        ) {
            Icon(
                imageVector = Icons.Filled.AddPhotoAlternate,
                contentDescription = "Unggah Gambar"
            )
        }

        // 3. Button Mulai Transliterasi
        Button(
            onClick = onProcessClick,
            modifier = Modifier
                .weight(1f) // Memenuhi sisa ruang di kanan
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.btnPrimary,
                disabledContainerColor = colors.btnPrimary.copy(alpha = 0.5f),
                disabledContentColor = colors.textOnPrimary.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = isProcessEnabled
        ) {
            Text(
                text = "Mulai Transliterasi",
                fontFamily = appFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                letterSpacing = 1.2.sp,
                color = if (isProcessEnabled) colors.textOnPrimary else colors.textOnPrimary.copy(alpha = 0.5f)
            )
        }
    }
}