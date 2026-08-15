package com.example.billease.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.billease.ui.bills.BillDetailScreen
import com.example.billease.ui.bills.BillFormScreen
import com.example.billease.ui.bills.BillsListScreen
import com.example.billease.ui.home.HomeScreen
import com.example.billease.ui.persons.PersonDetailScreen
import com.example.billease.ui.persons.PersonFormScreen
import com.example.billease.ui.persons.PersonsListScreen
import com.example.billease.ui.products.ProductFormScreen
import com.example.billease.ui.products.ProductsListScreen
import com.example.billease.ui.reports.ReportsScreen
import com.example.billease.ui.settings.SettingsScreen

@Suppress("FunctionNaming", "MagicNumber", "LongMethod", "MaxLineLength")
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // The routes where the bottom nav is visible
    val bottomNavRoutes = listOf("home", "reports", "bills_list", "products_list", "persons_list")
    val showBottomNav = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Reports") },
                        label = { Text("Reports") },
                        selected = currentRoute == "reports",
                        onClick = {
                            navController.navigate("reports") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors =
                            NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Menu, contentDescription = "Bills") },
                        label = { Text("Bills") },
                        selected = currentRoute == "bills_list",
                        onClick = {
                            navController.navigate("bills_list") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors =
                            NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                    )

                    // Invisible item to make space for the FAB in the center (optional, since NavigationBar just spaces evenly)
                    // If we want a true center FAB cutout, we would use BottomAppBar. Here we just rely on NavigationBar's spacing
                    // or add an empty item. For simplicity in Material 3, we just let it overlap if needed, or add a blank spacer.
                    NavigationBarItem(
                        icon = { },
                        label = { },
                        selected = false,
                        onClick = { },
                        enabled = false,
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Products") },
                        label = { Text("Products") },
                        selected = currentRoute == "products_list",
                        onClick = {
                            navController.navigate("products_list") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors =
                            NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Customers") },
                        label = { Text("Customers") },
                        selected = currentRoute == "persons_list",
                        onClick = {
                            navController.navigate("persons_list") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors =
                            NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            if (showBottomNav) {
                FloatingActionButton(
                    onClick = { navController.navigate("bill_form/-1") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Bill")
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
        ) {
            // ── Home Dashboard ────────────────────────────────────────────────────
            composable("home") {
                HomeScreen(
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToBills = { navController.navigate("bills_list") },
                )
            }

            // ── Reports Placeholder ───────────────────────────────────────────────
            composable("reports") {
                ReportsScreen()
            }

            // ── Settings ──────────────────────────────────────────────────────────
            composable("settings") {
                SettingsScreen(onNavigateBack = { navController.popBackStack() })
            }

            // ── Persons ───────────────────────────────────────────────────────────
            composable("persons_list") {
                PersonsListScreen(
                    onNavigateToPersonDetail = { navController.navigate("person_detail/$it") },
                    onNavigateToPersonForm = { id ->
                        navController.navigate("person_form/${id ?: -1L}")
                    },
                )
            }

            composable(
                route = "person_form/{personId}",
                arguments = listOf(navArgument("personId") { type = NavType.LongType }),
            ) {
                PersonFormScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(
                route = "person_detail/{personId}",
                arguments = listOf(navArgument("personId") { type = NavType.LongType }),
            ) {
                PersonDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { navController.navigate("person_form/$it") },
                    onNavigateToBillDetail = { navController.navigate("bill_detail/$it") },
                )
            }

            // ── Products ──────────────────────────────────────────────────────────
            composable("products_list") {
                ProductsListScreen(
                    onNavigateToProductForm = { id ->
                        navController.navigate("product_form/${id ?: -1L}")
                    },
                )
            }

            composable(
                route = "product_form/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.LongType }),
            ) {
                ProductFormScreen(onNavigateBack = { navController.popBackStack() })
            }

            // ── Bills ─────────────────────────────────────────────────────────────
            composable("bills_list") {
                BillsListScreen(
                    onNavigateToBillDetail = { navController.navigate("bill_detail/$it") },
                )
            }

            composable(
                route = "bill_form/{billId}",
                arguments = listOf(navArgument("billId") { type = NavType.LongType }),
            ) {
                BillFormScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(
                route = "bill_detail/{billId}",
                arguments = listOf(navArgument("billId") { type = NavType.LongType }),
            ) {
                BillDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { navController.navigate("bill_form/$it") },
                )
            }
        }
    }
}
