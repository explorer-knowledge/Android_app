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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "persons_list") {
        
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
    }
}
