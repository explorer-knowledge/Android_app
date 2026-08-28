package com.example.billease.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
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
import com.example.billease.ui.settings.SettingsViewModel
import com.example.billease.util.LocalCurrencyCode

@Suppress("MagicNumber", "LongMethod", "MaxLineLength")
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settings by settingsViewModel.appSettings.collectAsState()

    // The routes where the bottom nav is visible
    val bottomNavRoutes = listOf("home", "reports", "bills_list", "products_list", "persons_list")
    val showBottomNav = currentRoute in bottomNavRoutes

    val onNavigateToSettings = { navController.navigate("settings") }
    val onNavigateToBillForm = { navController.navigate("bill_form/-1") }

    CompositionLocalProvider(LocalCurrencyCode provides settings.currencyCode) {
        Scaffold(
            bottomBar = {
                if (showBottomNav) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ) {
                        BottomNavItem(
                            route = "reports",
                            currentRoute = currentRoute,
                            icon = Icons.Default.DateRange,
                            label = "Reports",
                            navController = navController,
                        )
                        BottomNavItem(
                            route = "bills_list",
                            currentRoute = currentRoute,
                            icon = Icons.Default.Menu,
                            label = "Bills",
                            navController = navController,
                        )
                        BottomNavItem(
                            route = "home",
                            currentRoute = currentRoute,
                            icon = Icons.Default.Home,
                            label = "Home",
                            navController = navController,
                        )
                        BottomNavItem(
                            route = "products_list",
                            currentRoute = currentRoute,
                            icon = Icons.Default.ShoppingCart,
                            label = "Products",
                            navController = navController,
                        )
                        BottomNavItem(
                            route = "persons_list",
                            currentRoute = currentRoute,
                            icon = Icons.Default.Person,
                            label = "Persons",
                            navController = navController,
                        )
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
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToBills = { navController.navigate("bills_list") },
                        onNavigateToProductForm = { navController.navigate("product_form/-1") },
                        onNavigateToBillForm = onNavigateToBillForm,
                        onNavigateToPersonForm = { navController.navigate("person_form/-1") },
                        onNavigateToBillDetail = { navController.navigate("bill_detail/$it") },
                    )
                }

                // ── Reports Placeholder ───────────────────────────────────────────────
                composable("reports") {
                    ReportsScreen(onNavigateToSettings = onNavigateToSettings)
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
                        onNavigateToSettings = onNavigateToSettings,
                    )
                }

                composable(
                    route = "person_form/{personId}",
                    arguments = listOf(navArgument("personId") { type = NavType.LongType }),
                ) {
                    PersonFormScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSettings = onNavigateToSettings,
                    )
                }

                composable(
                    route = "person_detail/{personId}",
                    arguments = listOf(navArgument("personId") { type = NavType.LongType }),
                ) {
                    PersonDetailScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEdit = { navController.navigate("person_form/$it") },
                        onNavigateToBillDetail = { navController.navigate("bill_detail/$it") },
                        onNavigateToSettings = onNavigateToSettings,
                    )
                }

                // ── Products ──────────────────────────────────────────────────────────
                composable("products_list") {
                    ProductsListScreen(
                        onNavigateToProductForm = { id ->
                            navController.navigate("product_form/${id ?: -1L}")
                        },
                        onNavigateToSettings = onNavigateToSettings,
                    )
                }

                composable(
                    route = "product_form/{productId}",
                    arguments = listOf(navArgument("productId") { type = NavType.LongType }),
                ) {
                    ProductFormScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSettings = onNavigateToSettings,
                    )
                }

                // ── Bills ─────────────────────────────────────────────────────────────
                composable("bills_list") {
                    BillsListScreen(
                        onNavigateToBillDetail = { navController.navigate("bill_detail/$it") },
                        onNavigateToBillForm = onNavigateToBillForm,
                        onNavigateToSettings = onNavigateToSettings,
                    )
                }

                composable(
                    route = "bill_form/{billId}",
                    arguments = listOf(navArgument("billId") { type = NavType.LongType }),
                ) {
                    BillFormScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSettings = onNavigateToSettings,
                    )
                }

                composable(
                    route = "bill_detail/{billId}",
                    arguments = listOf(navArgument("billId") { type = NavType.LongType }),
                ) {
                    BillDetailScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEdit = { navController.navigate("bill_form/$it") },
                        onNavigateToSettings = onNavigateToSettings,
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(
    route: String,
    currentRoute: String?,
    icon: ImageVector,
    label: String,
    navController: NavHostController,
) {
    NavigationBarItem(
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label) },
        selected = currentRoute == route,
        onClick = {
            navController.navigate(route) {
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
