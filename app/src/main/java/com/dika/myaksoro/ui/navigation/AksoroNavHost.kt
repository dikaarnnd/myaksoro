package com.dika.myaksoro.ui.navigation

// --- Android Framework & Graphics ---
import android.content.Context
import android.graphics.Bitmap

// --- Jetpack Compose: Foundation & Layout ---
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding

// --- Jetpack Compose: Runtime & State Management ---
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

// --- Jetpack Compose: UI ---
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily

// --- Jetpack Navigation ---
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

// --- Proyek Internal: Engine & Data ---
import com.dika.myaksoro.AksoroEngine
import com.dika.myaksoro.data.HistoryItem

// --- Proyek Internal: Screens & Theme ---
import com.dika.myaksoro.ui.screens.HistoryScreen
import com.dika.myaksoro.ui.screens.HomeScreen
import com.dika.myaksoro.ui.theme.AksoroColors

/**
 * Menampung seluruh state HomeScreen yang di-hoist agar tidak hilang saat pindah tab.
 */
class HomeState {
    var selectedBitmap by androidx.compose.runtime.mutableStateOf<Bitmap?>(null)
    var cnnResult by androidx.compose.runtime.mutableStateOf<List<String>>(emptyList())
    var chunkResult by androidx.compose.runtime.mutableStateOf<List<String>>(emptyList())
    var lstmResult by androidx.compose.runtime.mutableStateOf("...")
    var showProcessButton by androidx.compose.runtime.mutableStateOf(false)
    var isInferencing by androidx.compose.runtime.mutableStateOf(false)
}

@Composable
fun AksoroNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    engine: AksoroEngine,
    context: Context,
    colors: AksoroColors,
    appFont: FontFamily,
    historyList: MutableList<HistoryItem>,
    homeState: HomeState
) {
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
                selectedBitmap = homeState.selectedBitmap,
                onSelectedBitmapChange = { homeState.selectedBitmap = it },
                cnnResult = homeState.cnnResult,
                onCnnResultChange = { homeState.cnnResult = it },
                chunkResult = homeState.chunkResult,
                onChunkResultChange = { homeState.chunkResult = it },
                lstmResult = homeState.lstmResult,
                onLstmResultChange = { homeState.lstmResult = it },
                showProcessButton = homeState.showProcessButton,
                onShowProcessButtonChange = { homeState.showProcessButton = it },
                isInferencing = homeState.isInferencing,
                onInferencingChange = { homeState.isInferencing = it },
                appFont = appFont
            )
        }
        composable("history") {
            HistoryScreen(
                context = context,
                historyList = historyList,
                colors = colors,
                appFont = appFont
            )
        }
    }
}