package com.example.billease.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.billease.ui.persons.PersonDetailScreen
import com.example.billease.ui.persons.PersonFormScreen
import com.example.billease.ui.persons.PersonsListScreen
import com.example.billease.ui.products.ProductFormScreen
import com.example.billease.ui.products.ProductsListScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        
        composable("home") {
            // Temporary dashboard with buttons to navigate
            androidx.compose.foundation.layout.Column(
                modifier = androidx.compose.foundation.layout.Modifier.fillMaxSize(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Button(onClick = { navController.navigate("persons_list") }) {
                    androidx.compose.material3.Text("Persons")
                }
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.Modifier.height(16.dp))
                androidx.compose.material3.Button(onClick = { navController.navigate("products_list") }) {
                    androidx.compose.material3.Text("Products")
                }
            }
        }

        composable("persons_list") {
            PersonsListScreen(
                onNavigateToPersonDetail = { personId ->
                    navController.navigate("person_detail/$personId")
                },
                onNavigateToPersonForm = { personId ->
                    if (personId == null) {
                        navController.navigate("person_form/-1")
                    } else {
                        navController.navigate("person_form/$personId")
                    }
                }
            )
        }

        composable(
            route = "person_form/{personId}",
            arguments = listOf(navArgument("personId") { type = NavType.LongType })
        ) {
            PersonFormScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "person_detail/{personId}",
            arguments = listOf(navArgument("personId") { type = NavType.LongType })
        ) {
            PersonDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { personId ->
                    navController.navigate("person_form/$personId")
                },
                onNavigateToBillDetail = { billId ->
                    // Stub for Phase 5
                }
            )
        }
        
        composable("products_list") {
            ProductsListScreen(
                onNavigateToProductForm = { productId ->
                    if (productId == null) {
                        navController.navigate("product_form/-1")
                    } else {
                        navController.navigate("product_form/$productId")
                    }
                }
            )
        }

        composable(
            route = "product_form/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.LongType })
        ) {
            ProductFormScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
