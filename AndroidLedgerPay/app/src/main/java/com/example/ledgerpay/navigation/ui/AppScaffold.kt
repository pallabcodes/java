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
                        label = null
                    )
                }
            }
        }
    ) { _ ->
        AppNavGraph(navController = navController)
    }
}
