package com.example.inf2007_mad_j1847.view

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.inf2007_mad_j1847.viewmodel.AppointmentViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inf2007_mad_j1847.R
import com.example.inf2007_mad_j1847.components.TimeSlotPicker
import com.example.inf2007_mad_j1847.components.createTimestamp
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAppointmentScreen(
    navController: NavController,
    appointmentId: String,
    viewModel: AppointmentViewModel = viewModel()
) {
    val appointmentState = viewModel.selectedAppointment.collectAsState()
    val appointment = appointmentState.value

    var hospital by remember { mutableStateOf("") }
    var service by remember { mutableStateOf("") }
    var doctor by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var timeSlot by remember { mutableStateOf("") }

    var isUpdating by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(appointmentId) {
        viewModel.getAppointmentById(appointmentId)
    }

    // Populate Fields when Appointment Data is Loaded
    LaunchedEffect(appointment) {
        appointment?.let {
            hospital = it.location
            service = it.type
            doctor = it.doctor
            date = it.date?.toDate()?.let { date ->
                java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            } ?: ""
            timeSlot = it.date?.toDate()?.let { date ->
                java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            } ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Appointment") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (appointment == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            text = "Select a Hospital",
                            style = MaterialTheme.typography.labelMedium
                        )
                        ExposedDropdownMenuBox(hospital, viewModel.hospitals) { hospital = it }
                    }

                    item {
                        Text(
                            text = "Select Appointment Type",
                            style = MaterialTheme.typography.labelMedium
                        )
                        ExposedDropdownMenuBox(service, viewModel.services) { service = it }
                    }

                    item {
                        // Date Picker
                        Text(
                            text = "Select Appointment Date",
                            style = MaterialTheme.typography.labelMedium
                        )
                        DatePickerField(selectedDate = date) { date = it }
                    }

                    item {
                        // Time Slot Picker
                        if (date.isNotBlank()) {
                            Text(
                                text = "Select Appointment Time",
                                style = MaterialTheme.typography.labelMedium
                            )
                            TimeSlotPicker(selectedTimeSlot = timeSlot) { timeSlot = it }
                        }
                    }

                    item {
                        Text(text = "Select a Doctor", style = MaterialTheme.typography.labelMedium)
                        ExposedDropdownMenuBox(doctor, viewModel.doctors) { doctor = it }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                isUpdating = true

                                coroutineScope.launch {  // ✅ Use `coroutineScope.launch` here
                                    if (date.isBlank() || timeSlot.isBlank()) {
                                        showCustomToast(context, "⚠ Please select a date and time slot!")
                                        isUpdating = false
                                        return@launch
                                    }

                                    val timestamp = createTimestamp(date, timeSlot)

                                    if (timestamp == null) {
                                        showCustomToast(context, "⚠ Invalid date or time slot selected!")
                                        isUpdating = false
                                        return@launch
                                    }

                                    val updatedData = mutableMapOf<String, Any>(
                                        "location" to hospital,
                                        "type" to service,
                                        "doctor" to doctor
                                    )

                                    updatedData["date"] = timestamp

                                    viewModel.updateAppointment(
                                        appointmentId,
                                        updatedData,
                                        onSuccess = {
                                            showCustomToast(context, "✅ Appointment Updated!")
                                            isUpdating = false
                                            navController.popBackStack()
                                        },
                                        onFailure = { error ->
                                            showCustomToast(context, "❌ Update Failed: $error")
                                            isUpdating = false
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isUpdating
                        ) {
                            if (isUpdating) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                Text("Save Changes")
                            }
                        }
                    }
                }
            }
        }
    }
}

