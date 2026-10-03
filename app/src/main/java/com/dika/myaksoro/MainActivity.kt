package com.dika.myaksoro

// --- Android Framework & Graphics ---
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

// --- Jetpack Compose: Foundation & Layout ---
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold

// --- Jetpack Compose: Runtime & State Management ---
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// --- Jetpack Compose: Fonts & Navigation ---
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// --- Kotlin Coroutines ---
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// --- Proyek Internal: Data Layer ---
import com.dika.myaksoro.data.HistoryItem
import com.dika.myaksoro.data.HistoryManager

// --- Proyek Internal: UI Components & Navigation ---
import com.dika.myaksoro.ui.components.AksoroBottomBar
import com.dika.myaksoro.ui.components.AksoroTopBar
import com.dika.myaksoro.ui.navigation.AksoroNavHost
import com.dika.myaksoro.ui.navigation.HomeState

// --- Proyek Internal: Screens & Theme ---
import com.dika.myaksoro.ui.screens.SplashScreen
import com.dika.myaksoro.ui.theme.darkModeColors
import com.dika.myaksoro.ui.theme.lightModeColors

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

@Composable
fun AksoroMainApp(engine: AksoroEngine) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val interFont = FontFamily(Font(R.font.inter))

    var isDarkTheme by remember { mutableStateOf(false) }
    val colors = if (isDarkTheme) darkModeColors else lightModeColors

    var isSplashScreenVisible by remember { mutableStateOf(true) }

    val homeState = remember { HomeState() }
    val historyList = remember { mutableStateListOf<HistoryItem>() }

    LaunchedEffect(Unit) {
        val loadedHistory = withContext(Dispatchers.IO) {
            HistoryManager.loadHistory(context).also { list ->
                list.forEach { item ->
                    val file = File(item.imagePath)
                    if (file.exists()) {
                        item.bitmapCache = BitmapFactory.decodeFile(file.absolutePath)
                    }
                }
            }
        }
        historyList.addAll(loadedHistory)
    }

    if (isSplashScreenVisible) {
        SplashScreen(
            colors = colors,
            appFont = interFont,
            onSplashFinished = { isSplashScreenVisible = false },
        )
    } else {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        Scaffold(
            containerColor = colors.bgApp,
            topBar = {
                AksoroTopBar(
                    title = if (currentRoute == "history") "Semua Riwayat" else "Aksoro",
                    isDarkTheme = isDarkTheme,
                    colors = colors,
                    appFont = interFont,
                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                )
            },
            bottomBar = {
                Column {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = colors.textTertiary,
                    )
                    AksoroBottomBar(
                        navController = navController,
                        currentRoute = currentRoute,
                        colors = colors,
                        appFont = interFont,
                    )
                }
            },
        ) { paddingValues ->
            AksoroNavHost(
                navController = navController,
                paddingValues = paddingValues,
                engine = engine,
                context = context,
                colors = colors,
                appFont = interFont,
                historyList = historyList,
                homeState = homeState,
            )
        }
    }
}