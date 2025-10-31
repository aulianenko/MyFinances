package dev.aulianenko.myfinances.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
        // Main navigation screens use fade transitions
        composable(
            route = Screen.Dashboard.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            dev.aulianenko.myfinances.ui.screens.dashboard.DashboardScreen(
                onNavigateToAccountDetail = { accountId ->
                    navController.navigate(Screen.AccountDetail.createRoute(accountId))
                },
                onNavigateToBulkUpdate = {
                    navController.navigate(Screen.BulkUpdate.route)
                }
            )
        }

        composable(
            route = Screen.AccountList.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
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

        // Detail/Edit/Add screens use slide transitions
        composable(
            route = Screen.AddAccount.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
            dev.aulianenko.myfinances.ui.screens.account.AddAccountScreen(
                onNavigateBack = { navController.popBackStack() },
                onAccountSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditAccount.route,
            arguments = listOf(navArgument("accountId") { type = NavType.StringType }),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
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
            arguments = listOf(navArgument("accountId") { type = NavType.StringType }),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
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
            arguments = listOf(navArgument("accountId") { type = NavType.StringType }),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId") ?: return@composable
            dev.aulianenko.myfinances.ui.screens.accountvalue.AddAccountValueScreen(
                accountId = accountId,
                onNavigateBack = { navController.popBackStack() },
                onValueSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.BulkUpdate.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
            dev.aulianenko.myfinances.ui.screens.accountvalue.BulkUpdateScreen(
                onNavigateBack = { navController.popBackStack() },
                onValuesSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Settings.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            dev.aulianenko.myfinances.ui.screens.settings.SettingsScreen(
                onNavigateToCurrencyConverter = {
                    navController.navigate(Screen.CurrencyConverter.route)
                }
            )
        }

        composable(
            route = Screen.CurrencyConverter.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
            dev.aulianenko.myfinances.ui.screens.converter.CurrencyConverterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
