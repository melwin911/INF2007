package com.example.inf2007_mad_j1847

import com.example.inf2007_mad_j1847.view.ChatbotScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.inf2007_mad_j1847.screens.ProfileScreen
import com.example.inf2007_mad_j1847.view.AppointmentDetailsScreen
import com.example.inf2007_mad_j1847.view.AppointmentScreen
import com.example.inf2007_mad_j1847.view.AppointmentSelectionScreen
import com.example.inf2007_mad_j1847.view.BookAppointmentScreen
import com.example.inf2007_mad_j1847.view.HomeScreen
import com.example.inf2007_mad_j1847.view.LoginScreen
import com.example.inf2007_mad_j1847.view.SignUpScreen
import com.example.inf2007_mad_j1847.view.EditAppointmentScreen
import com.example.inf2007_mad_j1847.view.MapScreen
import com.example.inf2007_mad_j1847.view.QRScannerScreen

/**
 * The main navigation graph that manages screen routing.
 * Uses nested graphs to handle authentication flow separately from the main app flow.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val user = false // Change this based on authentication logic

    NavHost(
        navController = navController,
        startDestination = if (user) "main_graph" else "auth_graph", // Decide where to start based on login state
        modifier = modifier
    ) {
        // ──────────────────────────────── AUTH GRAPH ────────────────────────────────
        navigation(startDestination = "login_screen", route = "auth_graph") {
            composable("login_screen") { LoginScreen(navController) }
            composable("signup_screen") { SignUpScreen(navController) }
        }

        // ──────────────────────────────── MAIN GRAPH ────────────────────────────────
        navigation(startDestination = "home_screen", route = "main_graph") {
            composable("home_screen") { HomeScreen(navController) }
            composable("profile_screen") { ProfileScreen(navController) }
            composable("chatbot_screen") { ChatbotScreen(navController) }
            composable("book_appointment_screen") { BookAppointmentScreen(navController) }

            composable("map_screen") {
                MapScreen(navController)
            }

            composable("qr_scanner_screen") {
                QRScannerScreen(navController)
            }

            composable("appointments") {
                AppointmentScreen(navController)
            }

            // ───── DYNAMIC ROUTE: Appointment Details ─────
            composable(
                route = "appointment_details/{appointmentId}",
                arguments = listOf(
                    navArgument("appointmentId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
                AppointmentDetailsScreen(
                    navController = navController,
                    appointmentId = appointmentId
                )
            }

            // ───── DYNAMIC ROUTE: Edit Appointment ────
            composable("edit_appointment/{appointmentId}") { backStackEntry ->
                val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
                EditAppointmentScreen(navController, appointmentId)
            }

            // ───── DYNAMIC ROUTE: Appointment Selection ─────
            composable(
                route = "appointment_selection/{hospitalName}",
                arguments = listOf(
                    navArgument("hospitalName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val hospitalName = backStackEntry.arguments?.getString("hospitalName") ?: ""
                AppointmentSelectionScreen(
                    navController = navController,
                    hospitalName = hospitalName
                )
            }
        }
    }
}
