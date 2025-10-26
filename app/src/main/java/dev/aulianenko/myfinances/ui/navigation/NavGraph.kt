package dev.aulianenko.myfinances.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.AccountList.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            // TODO: Dashboard screen will be implemented later
            PlaceholderScreen(title = "Dashboard")
        }

        composable(Screen.AccountList.route) {
            dev.aulianenko.myfinances.ui.screens.account.AccountListScreen(
                onNavigateToAddAccount = { navController.navigate(Screen.AddAccount.route) },
                onNavigateToAccountDetail = { accountId ->
                    navController.navigate(Screen.AccountDetail.createRoute(accountId))
                }
            )
        }

        composable(Screen.AddAccount.route) {
            dev.aulianenko.myfinances.ui.screens.account.AddAccountScreen(
                onNavigateBack = { navController.popBackStack() },
                onAccountSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditAccount.route,
            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
        ) {
            // TODO: Edit Account screen will be implemented later
            PlaceholderScreen(title = "Edit Account")
        }

        composable(
            route = Screen.AccountDetail.route,
            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
        ) {
            // TODO: Account Detail screen will be implemented later
            PlaceholderScreen(title = "Account Detail")
        }

        composable(
            route = Screen.AddAccountValue.route,
            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
        ) {
            // TODO: Add Account Value screen will be implemented later
            PlaceholderScreen(title = "Add Account Value")
        }

        composable(Screen.Settings.route) {
            // TODO: Settings screen will be implemented later
            PlaceholderScreen(title = "Settings")
        }
    }
}
