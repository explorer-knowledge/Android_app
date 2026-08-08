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
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.Text


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        
        composable("home") {
            // Temporary dashboard with buttons to navigate
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { navController.navigate("persons_list") }) {
                    Text("Persons")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("products_list") }) {
                    Text("Products")
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
