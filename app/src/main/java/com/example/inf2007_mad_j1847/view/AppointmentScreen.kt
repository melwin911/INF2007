package com.example.inf2007_mad_j1847.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.inf2007_mad_j1847.model.Appointment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inf2007_mad_j1847.viewmodel.AppointmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentScreen(navController: NavController, viewModel: AppointmentViewModel = viewModel()) {
    val appointments by viewModel.appointments.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAppointments()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("View Appointments") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (errorMessage != null) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else if (appointments.isEmpty() && errorMessage == null) {
                Text(text = "No Appointments Found", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                    items(appointments.size) { index ->
                        val appointment = appointments[index]
                        AppointmentItem(appointment, navController)
                    }
                }
            }
        }
    }
}


@Composable
fun AppointmentItem(appointment: Appointment, navController: NavController){
    val formattedDate = appointment.date?.let { formatTimestamp(it) } ?: "N/A"
    val formattedCompletionTime = appointment.completion_time?.let { formatTimestamp(it) } ?: "N/A"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("appointment_details/${appointment.id}")
            }
            .padding(8.dp),
//        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "📅 Date: $formattedDate", fontWeight = FontWeight.Bold)
                if (appointment.status == "completed") {
                    Text(text = "✅ Completed Time: $formattedCompletionTime", color = Color.Green)
                }
                Text(text = "🩺 ${appointment.type}")
                Text(text = "🏥 ${appointment.location}", color = Color.Gray)
            }
        }
    }
}
