package com.example.billease.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import com.example.billease.ui.settings.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        // ── Home Dashboard ────────────────────────────────────────────────────
        composable("home") {
            HomeScreen(
                onNavigateToNewBill = { navController.navigate("bill_form/-1") },
                onNavigateToPersons = { navController.navigate("persons_list") },
                onNavigateToProducts = { navController.navigate("products_list") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToBills = { navController.navigate("bills_list") },
            )
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
                onNavigateToCreateBill = { navController.navigate("bill_form/-1") },
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
