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
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            dev.aulianenko.myfinances.ui.screens.dashboard.DashboardScreen(
                onNavigateToAccountDetail = { accountId ->
                    navController.navigate(Screen.AccountDetail.createRoute(accountId))
                },
                onNavigateToBulkUpdate = {
                    navController.navigate(Screen.BulkUpdate.route)
                }
            )
        }

        composable(Screen.AccountList.route) {
            dev.aulianenko.myfinances.ui.screens.account.AccountListScreen(
                onNavigateToAddAccount = { navController.navigate(Screen.AddAccount.route) },
                onNavigateToAccountDetail = { accountId ->
                    navController.navigate(Screen.AccountDetail.createRoute(accountId))
                },
                onNavigateToEditAccount = { accountId ->
                    navController.navigate(Screen.EditAccount.createRoute(accountId))
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
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: return@composable
            dev.aulianenko.myfinances.ui.screens.account.EditAccountScreen(
                accountId = accountId,
                onNavigateBack = { navController.popBackStack() },
                onAccountSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AccountDetail.route,
            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: return@composable
            dev.aulianenko.myfinances.ui.screens.account.AccountDetailScreen(
                accountId = accountId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddValue = { accountId ->
                    navController.navigate(Screen.AddAccountValue.createRoute(accountId))
                }
            )
        }

        composable(
            route = Screen.AddAccountValue.route,
            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: return@composable
            dev.aulianenko.myfinances.ui.screens.accountvalue.AddAccountValueScreen(
                accountId = accountId,
                onNavigateBack = { navController.popBackStack() },
                onValueSaved = { navController.popBackStack() }
            )
        }

        composable(Screen.BulkUpdate.route) {
            dev.aulianenko.myfinances.ui.screens.accountvalue.BulkUpdateScreen(
                onNavigateBack = { navController.popBackStack() },
                onValuesSaved = { navController.popBackStack() }
            )
        }

        composable(Screen.Analytics.route) {
            dev.aulianenko.myfinances.ui.screens.analytics.AnalyticsScreen()
        }

        composable(Screen.Settings.route) {
            // TODO: Settings screen will be implemented later
            PlaceholderScreen(title = "Settings")
        }
    }
}
