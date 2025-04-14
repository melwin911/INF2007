package com.example.inf2007_mad_j1847.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.inf2007_mad_j1847.R

/**
 * Main home screen of the app.
 * Acts as a navigation hub with a grid of feature buttons (Map, QR, Appointment, etc.)
 */

@Composable
fun HomeScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        Text(text = "Home Screen", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(40.dp))

        // 3 rows of features buttons arranged in a grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // First row: Map + QR Check-in
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HomeScreenButton(
                    title = "Find Hospitals",
                    icon = Icons.Default.LocationOn,
                    onClick = { navController.navigate("map_screen") }
                )
                HomeScreenButton(
                    title = "QR Check In",
                    icon = Icons.Default.CheckCircle,
                    onClick = { navController.navigate("qr_scanner_screen") }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Second row: Book + View Appointments
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HomeScreenButton(
                    title = "Book Appointment",
                    icon = Icons.Default.DateRange,
                    onClick = { navController.navigate("book_appointment_screen") }
                )
                HomeScreenButton(
                    title = "Appointment",
                    icon = Icons.Default.Person,
                    onClick = { navController.navigate("appointments") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Third row: Profile + Chatbot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HomeScreenButton(
                    title = "Profile",
                    icon = Icons.Default.Person,
                    onClick = { navController.navigate("profile_screen") }
                )

                ChatbotButton(navController) // Custom chatbot button with image icon
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Logout Button (last row)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HomeScreenButton(
                    title = "Logout",
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    onClick = { navController.navigate("login_screen") }
                )
            }
        }
    }
}

// Generic button used throughout the HomeScreen.
@Composable
fun HomeScreenButton(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .size(120.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EA))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 10.sp, color = Color.White)
        }
    }
}

// Chatbot button with custom image icon - Separated for unique styling (uses drawable instead of icon vector)
@Composable
fun ChatbotButton(navController: NavController) {
    Button(
        onClick = { navController.navigate("chatbot_screen") },
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .size(120.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EA))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.chatbot),
                contentDescription = "Chatbot",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Chatbot", fontSize = 10.sp, color = Color.White)
        }
    }
}
