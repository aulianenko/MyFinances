package dev.aulianenko.myfinances.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : BottomNavItem(
        route = Screen.Dashboard.route,
        title = "Dashboard",
        icon = Icons.Default.Home
    )

    data object Accounts : BottomNavItem(
        route = Screen.AccountList.route,
        title = "Accounts",
        icon = Icons.Default.List
    )

    data object Settings : BottomNavItem(
        route = Screen.Settings.route,
        title = "Settings",
        icon = Icons.Default.Settings
    )

    companion object {
        val items = listOf(Dashboard, Accounts, Settings)
    }
}
