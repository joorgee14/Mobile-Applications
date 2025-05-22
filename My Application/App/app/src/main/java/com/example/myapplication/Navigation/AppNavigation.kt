package com.example.myapplication.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.screens.*


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppScreens.Login.route) {
        composable(route = AppScreens.Login.route) {
            LoginScreen(navController)
        }
        composable(
            route = "dashboard/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            MainDashboard(navController, userId)
        }
        composable(
            route = AppScreens.Progress.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ProgressScreen(
                navController = navController,
                userId = userId,
                onBackToHome = { navController.popBackStack() }
            )
        }

        composable(route = AppScreens.Register.route) {
            RegisterScreen(navController)
        }
        composable(
            route = AppScreens.MySessions.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            MySessionsScreen(navController, userId)
        }
        composable(route = AppScreens.GymsMap.route) {
            GymsMapScreen(navController)
        }
        composable(route = "editProfile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            EditProfileScreen(navController, userId)
        }

    }
}
