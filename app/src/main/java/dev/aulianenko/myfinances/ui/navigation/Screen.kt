package dev.aulianenko.myfinances.ui.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object AccountList : Screen("account_list")
    data object AddAccount : Screen("add_account")
    data object EditAccount : Screen("edit_account/{accountId}") {
        fun createRoute(accountId: String) = "edit_account/$accountId"
    }
    data object AccountDetail : Screen("account_detail/{accountId}") {
        fun createRoute(accountId: String) = "account_detail/$accountId"
    }
    data object AddAccountValue : Screen("add_account_value/{accountId}") {
        fun createRoute(accountId: String) = "add_account_value/$accountId"
    }
    data object Settings : Screen("settings")
}