package com.dika.myaksoro.ui.components

// --- Jetpack Compose: Material Icons ---
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List

// --- Jetpack Compose: Material 3 ---
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text

// --- Jetpack Compose: Runtime & UI ---
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// --- Jetpack Navigation ---
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

// --- Proyek Internal: Theme ---
import com.dika.myaksoro.ui.theme.AksoroColors

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val navItems = listOf(
    NavItem("home", "Beranda", Icons.Filled.Home),
    NavItem("history", "Riwayat", Icons.Filled.List)
)

@Composable
fun AksoroBottomBar(
    navController: NavHostController,
    currentRoute: String?,
    colors: AksoroColors,
    appFont: FontFamily
) {
    NavigationBar(
        containerColor = colors.bgNavBar,
        tonalElevation = 8.dp
    ) {
        navItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = {
                    Text(item.label, fontFamily = appFont, fontWeight = FontWeight.Medium)
                },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
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
}