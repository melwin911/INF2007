package com.example.inf2007_mad_j1847.view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inf2007_mad_j1847.viewmodel.AppointmentViewModel
import android.view.Gravity
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.inf2007_mad_j1847.model.Appointment
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailsScreen(
    navController: NavController,
    appointmentId: String,
    viewModel: AppointmentViewModel = viewModel()
) {
    // Fetch appointment details from ViewModel instead of direct Firebase access
    val appointments by viewModel.appointments.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val successMessage by viewModel.successMessage.collectAsState()
    val context = LocalContext.current

    // Fetch appointment details when the screen loads
    LaunchedEffect(appointmentId) {
        viewModel.fetchAppointments()
    }

    LaunchedEffect(successMessage) {
        val message = successMessage
        if (!message.isNullOrEmpty()) {
            showCustomToast(context, message)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointment Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                errorMessage != null -> {
                    Text(
                        text = "Error: $errorMessage",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    // Find the selected appointment from ViewModel
                    val selectedAppointment = appointments.find { it.id == appointmentId }

                    selectedAppointment?.let { appt ->
                        val scrollState = rememberScrollState()
                        val isEnabled = appt.status == "upcoming"

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Status Badge
                            val statusColor = when (appt.status) {
                                "completed" -> Color(0xFF4CAF50) // Green
                                "missed" -> Color(0xFFF44336) // Red
                                else -> Color(0xFF2196F3) // Blue for upcoming
                            }

                            val statusIcon = when (appt.status) {
                                "completed" -> "✅"
                                "missed" -> "⚠️"
                                else -> "⏳"
                            }

                            Surface(
                                color = statusColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Text(
                                    text = "$statusIcon ${appt.status.capitalize(Locale.getDefault())}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }

                            // Appointment Info Card
                            AppointmentInfoCard(appt)

                            // QR Code or Completion Info
                            if (appt.status == "upcoming") {
                                AppointmentQRSection(appt.id)
                            } else {
                                CompletionInfoSection(appt)
                            }

                            Row (
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ){
                                // Edit Button
                                Button(
                                    onClick = {
                                        viewModel.getAppointmentById(appt.id)
                                        navController.navigate("edit_appointment/${appt.id}") },
                                    enabled = isEnabled,
                                    modifier = Modifier.padding(top = 16.dp),
                                ) {
                                    Text("Edit")
                                }

                                // Delete Logic
                                var showDialog by remember { mutableStateOf(false) }

                                if (showDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showDialog = false },
                                        title = { Text("Confirm Delete") },
                                        text = { Text("Are you sure you want to delete this appointment? This action cannot be undone.") },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    showDialog = false
                                                    viewModel.deleteAppointment(
                                                        appointmentId,
                                                        onSuccess = {
                                                            showCustomToast(context, "✅ Appointment Deleted!")
                                                            navController.popBackStack() // Navigate back after deletion
                                                        },
                                                        onFailure = { errorMessage ->
                                                            showCustomToast(context, "❌ Deletion Failed: $errorMessage")
                                                        }
                                                    )
                                                }
                                            ) {
                                                Text("Delete", color = Color.White)
                                            }
                                        },
                                        dismissButton = {
                                            Button(onClick = { showDialog = false }) {
                                                Text("Cancel")
                                            }
                                        }
                                    )
                                }

                                Button(
                                    onClick = { showDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    enabled = isEnabled,
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Text("Delete")
                                }
                            }



                        }
                    } ?: run {
                        Text(text = "Appointment not found", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentInfoCard(appointment: Appointment) {
    val formattedDate = appointment.date?.let { formatTimestamp(it) } ?: "N/A"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            InfoRow(Icons.Default.Person, "Doctor", appointment.doctor)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            InfoRow(Icons.Default.LocationOn, "Hospital", appointment.location)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            InfoRow(Icons.Default.DateRange, "Date", formattedDate)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            InfoRow(Icons.Default.Person, "Appointment Type", appointment.type)
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AppointmentQRSection(appointmentId: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Appointment ID", style = MaterialTheme.typography.titleMedium)
            Text(text = appointmentId, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 16.dp))
            Text(text = "Please show this ID at the check-in counter", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CompletionInfoSection(appointment: Appointment) {

    val formattedCompletionTime = appointment.completion_time?.let { formatTimestamp(it) } ?: "N/A"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val statusText = if (appointment.status == "completed") "Completion Details" else "Missed Appointment"
            Text(text = statusText, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            InfoRow(Icons.Default.DateRange, if (appointment.status == "completed") "Check-in Time" else "Missed Time", formattedCompletionTime)
        }
    }
}

fun formatTimestamp(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(date)
}