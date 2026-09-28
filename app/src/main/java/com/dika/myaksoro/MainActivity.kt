package com.dika.myaksoro

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dika.myaksoro.data.HistoryItem
import com.dika.myaksoro.data.HistoryManager
import com.dika.myaksoro.ui.components.AksoroBottomBar
import com.dika.myaksoro.ui.components.AksoroTopBar
import com.dika.myaksoro.ui.navigation.AksoroNavHost
import com.dika.myaksoro.ui.navigation.HomeState
import com.dika.myaksoro.ui.theme.darkModeColors
import com.dika.myaksoro.ui.theme.lightModeColors
import kotlinx.coroutines.Dispatchers
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

@Composable
fun AksoroMainApp(engine: AksoroEngine) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val interFont = FontFamily(Font(R.font.inter))

    var isDarkTheme by remember { mutableStateOf(false) }
    val colors = if (isDarkTheme) darkModeColors else lightModeColors

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
                onToggleTheme = { isDarkTheme = !isDarkTheme }
            )
        },
        bottomBar = {
            AksoroBottomBar(
                navController = navController,
                currentRoute = currentRoute,
                colors = colors,
                appFont = interFont
            )
        }
    ) { paddingValues ->
        AksoroNavHost(
            navController = navController,
            paddingValues = paddingValues,
            engine = engine,
            context = context,
            colors = colors,
            appFont = interFont,
            historyList = historyList,
            homeState = homeState
        )
    }
}