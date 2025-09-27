package com.example.ledgerpay.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

sealed class Dest(val route: String) {
    data object Home: Dest("home")
    data object Payments: Dest("payments")
    data object Ledger: Dest("ledger")
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Dest.Home.route) {
        composable(Dest.Home.route) { com.example.ledgerpay.navigation.ui.HomeScreen { navController.navigate(Dest.Payments.route) } }
        composable(Dest.Payments.route) { com.example.ledgerpay.feature.payments.ui.PaymentsScreen() }
        composable(Dest.Ledger.route) { com.example.ledgerpay.feature.ledger.LedgerScreen() }
    }
}
