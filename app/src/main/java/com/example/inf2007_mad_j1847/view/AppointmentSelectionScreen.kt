package com.example.inf2007_mad_j1847.view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inf2007_mad_j1847.model.Appointment
import com.example.inf2007_mad_j1847.viewmodel.AppointmentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentSelectionScreen(
    navController: NavController,
    hospitalName: String,
    viewModel: AppointmentViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // state for UI
    var isLoading by remember { mutableStateOf(true) }
    var todayAppointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var selectedAppointments by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isCheckingIn by remember { mutableStateOf(false) }

    // load appointments when screen is created
    LaunchedEffect(hospitalName) {
        viewModel.getTodayAppointmentsAtLocation(
            hospitalName = hospitalName,
            onSuccess = { appointments ->
                todayAppointments = appointments
                isLoading = false
            },
            onFailure = { error ->
                showCustomToast(context, "❌ $error")
                isLoading = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Check-In at $hospitalName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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

                todayAppointments.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No upcoming appointments found for today at $hospitalName",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text("Go Back")
                        }
                    }
                }

                else -> {
                    Column {
                        Text(
                            text = "Your Appointments Today",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Select appointments to check in",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(todayAppointments.size) { index ->
                                val appointment = todayAppointments[index]
                                val isSelected = selectedAppointments.contains(appointment.id)

                                AppointmentCheckInCard(
                                    appointment = appointment,
                                    isSelected = isSelected,
                                    onToggleSelection = {
                                        selectedAppointments = if (isSelected) {
                                            selectedAppointments - appointment.id
                                        } else {
                                            selectedAppointments + appointment.id
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // batch check-in button
                        Button(
                            onClick = {
                                if (selectedAppointments.isEmpty()) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Please select at least one appointment")
                                    }
                                    return@Button
                                }

                                isCheckingIn = true

                                // Convert set to list
                                val appointmentIdsToCheckIn = selectedAppointments.toList()

                                // Use batch check-in
                                viewModel.batchCheckInAppointments(
                                    appointmentIds = appointmentIdsToCheckIn,
                                    onProgress = { current, total ->
                                    },
                                    onComplete = { successful, failed ->
                                        isCheckingIn = false


                                        if (successful > 0) {
                                            val message = if (failed > 0) {
                                                "✅ Checked in $successful appointments (failed: $failed)"
                                            } else {
                                                "✅ Successfully checked in $successful appointments"
                                            }
                                            showCustomToast(context, message)

                                            // nav back to appointments list
                                            navController.navigate("appointments") {
                                                popUpTo("home_screen")
                                            }
                                        } else {
                                            showCustomToast(context, "❌ Failed to check in any appointments")
                                        }
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = selectedAppointments.isNotEmpty() && !isCheckingIn
                        ) {
                            if (isCheckingIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Checking in...")
                            } else {
                                Text(
                                    text = if (selectedAppointments.size > 1)
                                        "Check In (${selectedAppointments.size} appointments)"
                                    else if (selectedAppointments.size == 1)
                                        "Check In (1 appointment)"
                                    else
                                        "Check In"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentCheckInCard(
    appointment: Appointment,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    val formattedDate = appointment.date?.let { formatTimestamp(it) } ?: "N/A"

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleSelection) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Star,
                    contentDescription = if (isSelected) "Deselect" else "Select",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // appointment details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = appointment.type,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Time",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "With Dr. ${appointment.doctor}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}