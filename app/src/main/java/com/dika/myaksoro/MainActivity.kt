package com.dika.myaksoro

import com.dika.myaksoro.ui.theme.AksoroColors
import com.dika.myaksoro.ui.theme.lightModeColors
import com.dika.myaksoro.ui.theme.darkModeColors
import com.dika.myaksoro.ui.screens.HomeScreen
import com.dika.myaksoro.ui.screens.HistoryScreen
import com.dika.myaksoro.data.HistoryManager
import com.dika.myaksoro.data.HistoryItem

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AksoroMainApp(engine: AksoroEngine) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val interFont = FontFamily(Font(R.font.inter))

    var isDarkTheme by remember { mutableStateOf(false) }
    val colors = if (isDarkTheme) darkModeColors else lightModeColors

    // --- STATE HOISTING ---
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cnnResult by remember { mutableStateOf<List<String>>(emptyList()) }
    var chunkResult by remember { mutableStateOf<List<String>>(emptyList()) }
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
                        fontFamily = interFont,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary,
                        fontSize = 22.sp,
                        letterSpacing = (-0.5).sp
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
                    label = { Text("Beranda", fontFamily = interFont, fontWeight = FontWeight.Medium) },
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
                    label = { Text("Riwayat", fontFamily = interFont, fontWeight = FontWeight.Medium) },
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
                    chunkResult = chunkResult,
                    onChunkResultChange = { chunkResult = it },
                    lstmResult = lstmResult,
                    onLstmResultChange = { lstmResult = it },
                    showProcessButton = showProcessButton,
                    onShowProcessButtonChange = { showProcessButton = it },
                    isInferencing = isInferencing,
                    onInferencingChange = { isInferencing = it },
                    appFont = interFont
                )
            }
            composable("history") {
                HistoryScreen(
                    context = context,
                    historyList = historyList,
                    colors = colors,
                    appFont = interFont
                )
            }
        }
    }
}