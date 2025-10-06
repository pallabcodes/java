package com.example.ledgerpay.navigation.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ledgerpay.navigation.AppNavGraph
import com.example.ledgerpay.navigation.Dest

@Composable
fun AppScaffold() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(Dest.Home, Dest.Payments, Dest.Ledger).forEach { dest ->
                    NavigationBarItem(
                        selected = route == dest.route,
                        onClick = { if (route != dest.route) navController.navigate(dest.route) },
                        icon = { Icon(painterResource(android.R.drawable.ic_menu_agenda), contentDescription = dest.route) },
                        label = { androidx.compose.material3.Text(text = when(dest){ is com.example.ledgerpay.navigation.Dest.Home -> androidx.compose.ui.res.stringResource(id = com.example.ledgerpay.R.string.tab_home); is com.example.ledgerpay.navigation.Dest.Payments -> androidx.compose.ui.res.stringResource(id = com.example.ledgerpay.R.string.tab_payments); else -> androidx.compose.ui.res.stringResource(id = com.example.ledgerpay.R.string.tab_ledger) }) }
                    )
                }
            }
        }
    ) { _ ->
        AppNavGraph(navController = navController)
    }
}
