package com.dika.myaksoro

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

// ===========================================
// 1. DATA CLASS & MANAJER PENYIMPANAN
// ===========================================
data class HistoryItem(
    val imagePath: String,
    val cnnOutput: List<String>,
    val lstmOutput: String,
    var bitmapCache: Bitmap? = null
)

object HistoryManager {
    private const val PREFS_NAME = "aksoro_history"
    private const val KEY_HISTORY = "history_data"

    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String {
        val filename = "history_${System.currentTimeMillis()}.png"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file.absolutePath
    }

    fun saveHistory(context: Context, historyList: List<HistoryItem>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        historyList.forEach { item ->
            val jsonObj = JSONObject().apply {
                put("imagePath", item.imagePath)
                put("cnnOutput", item.cnnOutput.joinToString(","))
                put("lstmOutput", item.lstmOutput)
            }
            jsonArray.put(jsonObj)
        }
        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
    }

    fun loadHistory(context: Context): List<HistoryItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
        val historyList = mutableListOf<HistoryItem>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObj = jsonArray.getJSONObject(i)
                val imagePath = jsonObj.getString("imagePath")
                val cnnOutputString = jsonObj.getString("cnnOutput")
                val cnnOutput = if (cnnOutputString.isNotEmpty()) cnnOutputString.split(",") else emptyList()
                val lstmOutput = jsonObj.getString("lstmOutput")
                historyList.add(HistoryItem(imagePath, cnnOutput, lstmOutput))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return historyList
    }
}

// ==========================================
// 2. MAIN ACTIVITY
// ==========================================
class MainActivity : ComponentActivity() {
    private lateinit var aksoroEngine: AksoroEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        aksoroEngine = AksoroEngine(this)
        setContent {
            AksoroMainApp(aksoroEngine)
        }
    }
}

// ==========================================
// 3. PENGATURAN TEMA & PALET WARNA (MONOCHROME)
// ==========================================
data class AksoroColors(
    val bgApp: Color,
    val bgTopBar: Color,
    val bgNavBar: Color,
    val btnPrimary: Color,
    val btnSecondary: Color,
    val btnAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textOnPrimary: Color,
    val textOnSecondary: Color,
    val boxHistoryPrimary: Color,
    val boxHistorySecondary: Color,
    val cardBg: Color,
    val overlayBg: Color
)

val lightModeColors = AksoroColors(
    bgApp = Color(0xFFF5F5F5),
    bgTopBar = Color(0xFFFFFFFF),
    bgNavBar = Color(0xFFFFFFFF),
    btnPrimary = Color(0xFF1E1E1E),
    btnSecondary = Color(0xFFE0E0E0),
    btnAccent = Color(0xFF1E1E1E),
    textPrimary = Color(0xFF121212),
    textSecondary = Color(0xFF616161),
    textTertiary = Color(0xFF9E9E9E),
    textOnPrimary = Color(0xFFFFFFFF),
    textOnSecondary = Color(0xFF121212),
    boxHistoryPrimary = Color(0xFFFFFFFF),
    boxHistorySecondary = Color(0xFFEEEEEE),
    cardBg = Color(0xFFFFFFFF),
    overlayBg = Color(0xD9000000)
)

val darkModeColors = AksoroColors(
    bgApp = Color(0xFF121212),
    bgTopBar = Color(0xFF1E1E1E),
    bgNavBar = Color(0xFF1E1E1E),
    btnPrimary = Color(0xFFEEEEEE),
    btnSecondary = Color(0xFF333333),
    btnAccent = Color(0xFFEEEEEE),
    textPrimary = Color(0xFFEEEEEE),
    textSecondary = Color(0xFFAAAAAA),
    textTertiary = Color(0xFF777777),
    textOnPrimary = Color(0xFF121212),
    textOnSecondary = Color(0xFFEEEEEE),
    boxHistoryPrimary = Color(0xFF1E1E1E),
    boxHistorySecondary = Color(0xFF2C2C2C),
    cardBg = Color(0xFF1E1E1E),
    overlayBg = Color(0xD9000000)
)

// ==========================================
// 4. APLIKASI UTAMA (SCAFFOLD & NAVIGASI)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AksoroMainApp(engine: AksoroEngine) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val manropeFont = FontFamily(Font(R.font.manrope))

    var isDarkTheme by remember { mutableStateOf(false) }
    val colors = if (isDarkTheme) darkModeColors else lightModeColors

    // --- STATE HOISTING ---
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cnnResult by remember { mutableStateOf<List<String>>(emptyList()) }
    var lstmResult by remember { mutableStateOf("...") }
    var showProcessButton by remember { mutableStateOf(false) }
    var isInferencing by remember { mutableStateOf(false) }
    val historyList = remember { mutableStateListOf<HistoryItem>() }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val loadedHistory = HistoryManager.loadHistory(context)
            loadedHistory.forEach { item ->
                val file = File(item.imagePath)
                if (file.exists()) {
                    item.bitmapCache = BitmapFactory.decodeFile(file.absolutePath)
                }
            }
            withContext(Dispatchers.Main) {
                historyList.addAll(loadedHistory)
            }
        }
    }

    Scaffold(
        containerColor = colors.bgApp,
        topBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val titleText = if (currentRoute == "history") "Semua Riwayat" else "Aksoro"

            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = titleText,
                        fontFamily = manropeFont,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary,
                        fontSize = 22.sp
                    )
                },
                actions = {
                    IconButton(onClick = { isDarkTheme = !isDarkTheme }) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Filled.Brightness7 else Icons.Filled.Brightness4,
                            contentDescription = "Toggle Theme",
                            tint = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colors.bgTopBar
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = colors.bgNavBar,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Beranda") },
                    label = { Text("Beranda", fontFamily = manropeFont) },
                    selected = currentRoute == "home",
                    onClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.btnPrimary,
                        selectedTextColor = colors.btnPrimary,
                        indicatorColor = colors.btnSecondary,
                        unselectedIconColor = colors.textTertiary,
                        unselectedTextColor = colors.textTertiary
                    )
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = "Riwayat") },
                    label = { Text("Riwayat", fontFamily = manropeFont) },
                    selected = currentRoute == "history",
                    onClick = {
                        navController.navigate("history") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.btnPrimary,
                        selectedTextColor = colors.btnPrimary,
                        indicatorColor = colors.btnSecondary,
                        unselectedIconColor = colors.textTertiary,
                        unselectedTextColor = colors.textTertiary
                    )
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(
                    engine = engine,
                    context = context,
                    colors = colors,
                    historyList = historyList,
                    selectedBitmap = selectedBitmap,
                    onSelectedBitmapChange = { selectedBitmap = it },
                    cnnResult = cnnResult,
                    onCnnResultChange = { cnnResult = it },
                    lstmResult = lstmResult,
                    onLstmResultChange = { lstmResult = it },
                    showProcessButton = showProcessButton,
                    onShowProcessButtonChange = { showProcessButton = it },
                    isInferencing = isInferencing,
                    onInferencingChange = { isInferencing = it },
                    manropeFont = manropeFont
                )
            }
            composable("history") {
                HistoryScreen(
                    context = context,
                    historyList = historyList,
                    colors = colors,
                    manropeFont = manropeFont
                )
            }
        }
    }
}

// ==========================================
// 5. SCREEN BERANDA (HOME)
// ==========================================
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
    lstmResult: String,
    onLstmResultChange: (String) -> Unit,
    showProcessButton: Boolean,
    onShowProcessButtonChange: (Boolean) -> Unit,
    isInferencing: Boolean,
    onInferencingChange: (Boolean) -> Unit,
    manropeFont: FontFamily
) {
    val coroutineScope = rememberCoroutineScope()

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
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("HASIL KLASIFIKASI", fontFamily = manropeFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                    Text(if (cnnResult.isEmpty()) "[]" else cnnResult.toString(), fontFamily = manropeFont, fontSize = 14.sp, color = colors.textPrimary, modifier = Modifier.padding(top = 4.dp))

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("HASIL TRANSLASI", fontFamily = manropeFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                    Text(lstmResult, fontFamily = manropeFont, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.padding(top = 8.dp))
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

                        coroutineScope.launch(Dispatchers.IO) {
                            val engineResult = engine.processImage(selectedBitmap!!)
                            val debugImg = engineResult.debugImage ?: selectedBitmap!!

                            val savedPath = HistoryManager.saveBitmapToInternalStorage(context, debugImg)
                            val newItem = HistoryItem(savedPath, engineResult.cnnOutput, engineResult.lstmOutput, debugImg)

                            withContext(Dispatchers.Main) {
                                onSelectedBitmapChange(debugImg)
                                onCnnResultChange(engineResult.cnnOutput)
                                onLstmResultChange(engineResult.lstmOutput)

                                historyList.add(0, newItem)
                                HistoryManager.saveHistory(context, historyList)

                                onInferencingChange(false)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.btnAccent),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Mulai Translasi AI", fontFamily = manropeFont, fontWeight = FontWeight.Bold, color = colors.textOnPrimary)
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
                                Text(
                                    text = if (item.cnnOutput.isEmpty()) "[]" else item.cnnOutput.toString(),
                                    fontFamily = manropeFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.lstmOutput,
                                    fontFamily = manropeFont, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary
                                )
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(60.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Translating script...", fontFamily = manropeFont, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Applying AI models to preserve heritage.", fontFamily = manropeFont, color = Color.LightGray, fontSize = 14.sp)
                }
            }
        }
    }
}

// ==========================================
// 6. SCREEN RIWAYAT PENUH (HISTORY)
// ==========================================
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
                            .background(Color(0x1AD32F2F), RoundedCornerShape(12.dp))
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
                                    Text(
                                        text = if (item.cnnOutput.isEmpty()) "[]" else item.cnnOutput.toString(),
                                        fontFamily = manropeFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = item.lstmOutput,
                                        fontFamily = manropeFont, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary
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