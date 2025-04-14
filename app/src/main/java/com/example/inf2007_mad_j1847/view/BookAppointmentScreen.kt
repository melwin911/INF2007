package com.example.inf2007_mad_j1847.view

import android.content.Context
import android.content.SharedPreferences
import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.runtime.rememberCoroutineScope
import com.example.inf2007_mad_j1847.components.TimeSlotPicker
import com.example.inf2007_mad_j1847.model.Appointment
import com.example.inf2007_mad_j1847.components.createTimestamp
import kotlinx.coroutines.launch
import java.util.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.inf2007_mad_j1847.viewmodel.AppointmentViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.inf2007_mad_j1847.R
import com.example.inf2007_mad_j1847.utils.SoundUtils


/**
 * BookAppointmentScreen.kt
 *
 * This screen allows users to book medical appointments. It fetches user information from Firebase
 * and handles form submission with input validation, custom toasts notifications, and feedback sounds.
 * The appointment details are saved to Firestore via the AppointmentViewModel.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(navController: NavController, viewModel: AppointmentViewModel = viewModel()) {

    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    // Form field states
    var hospital by remember { mutableStateOf("") }
    var service by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var timeSlot by remember { mutableStateOf("") }
    var doctor by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("Patient") } // Default username
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current

    val userId = auth.currentUser?.uid ?: "" // Current user UID from Firebase Auth

    // Fetch username from Firebase when userId is available
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            try {
                val userDoc = FirebaseFirestore.getInstance().collection("users").document(userId).get().await()
                val fetchedUsername = userDoc.getString("username")
                if (!fetchedUsername.isNullOrBlank()) {
                    username = fetchedUsername
                }
            } catch (e: Exception) {
                Log.e("BookAppointmentScreen", "Error fetching username", e)
            }
        }
    }


    // State to show loading indicator during async ops
    val isLoading by viewModel.loading.collectAsState()

    // Static dropdown options
    val hospitals = listOf("Sengkang Community Hospital", "Singapore General Hospital", "National University Hospital", "Khoo Teck Puat Hospital", "Mount Elizabeth Novena Hospital", "Changi General Hospital", "Dr Beef's Clinic")
    val services = listOf("General Checkup", "Dental Checkup", "Cardiology", "Dermatology", "Orthopedics")
    val doctors = listOf("Dr. Smith", "Dr. Lee", "Dr. Alex")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Appointment") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        // Scrollable column of form inputs
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                SnackbarHost(hostState = snackbarHostState)
                Spacer(modifier = Modifier.height(16.dp))

                // Hospital Dropdown with Label
                Text(text = "Select a Hospital", style = MaterialTheme.typography.labelMedium)
                ExposedDropdownMenuBox(hospital, hospitals) { hospital = it }

                Spacer(modifier = Modifier.height(16.dp))

                // Service Dropdown with Label
                Text(text = "Select Appointment Type", style = MaterialTheme.typography.labelMedium)
                ExposedDropdownMenuBox(service, services) { service = it }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Picker with Label
                Text(text = "Select Appointment Date", style = MaterialTheme.typography.labelMedium)
                DatePickerField(selectedDate = date) { date = it }

                Spacer(modifier = Modifier.height(16.dp))

                // Time slot picker - appears only after date is picked
                if (date.isNotBlank()) {
                    Text(text = "Select Appointment Time", style = MaterialTheme.typography.labelMedium)
                    TimeSlotPicker(selectedTimeSlot = timeSlot) { timeSlot = it }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Doctor Dropdown with Label
                Text(text = "Select a Doctor", style = MaterialTheme.typography.labelMedium)
                ExposedDropdownMenuBox(doctor, doctors) { doctor = it }

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                // Submit Button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            // Input Fields Validation
                            if (hospital.isBlank() || service.isBlank() || date.isBlank() || doctor.isBlank() || timeSlot.isBlank()) {
                                showCustomToast(context, "⚠ Please fill in all fields!")
                                return@launch
                            }

                            val timestamp = createTimestamp(date, timeSlot) // convert to Firebase-compatible timestamp

                            // Prevent null timestamps from causing crashes
                            if (timestamp == null) {
                                showCustomToast(context, "⚠ Invalid date selected!")
                                return@launch
                            }

                            // Create appointment object
                            val appointment = Appointment(
                                id = "",
                                date = timestamp,
                                doctor = doctor,
                                location = hospital,
                                type = service,
                                username = username,
                                status = "upcoming",
                                completion_time = null,
                                uid = userId
                            )

                            viewModel.bookAppointment(appointment,
                                onSuccess = {
                                    showCustomToast(context, "✅ Appointment Booked!")
                                    navController.popBackStack()
                                },
                                onFailure = { error ->
                                    showCustomToast(context, "❌ Error: $error")
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text(text = "Confirm Appointment")
                }
            }
        }
    }
}

// Dropdown Menu Component
@Composable
fun ExposedDropdownMenuBox(selectedValue: String, options: List<String>, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            label = { Text("Select an option") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Date picker field
@Composable
fun DatePickerField(selectedDate: String, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    // Set minimum date to tmr
    calendar.add(Calendar.DAY_OF_MONTH, 1)
    val minDate = calendar.timeInMillis

    fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                onDateSelected(formattedDate)
            },
            year, month, day
        )

        // Disable today's date by setting minDate to tomorrow
        datePickerDialog.datePicker.minDate = minDate
        datePickerDialog.show()
    }

    OutlinedTextField(
        value = selectedDate,
        onValueChange = {},
        label = { Text("Select Date") },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker() },
        trailingIcon = {
            IconButton(onClick = { showDatePicker() }) {
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Date")
            }
        }
    )
}

// Custom Toast Notifications (top center)
fun showCustomToast(context: Context, message: String) {
    val inflater = LayoutInflater.from(context)
    val layout: View = inflater.inflate(R.layout.custom_toast, null)

    val text: TextView = layout.findViewById(R.id.toast_text)
    text.text = message

    val toast = Toast(context)
    toast.duration = Toast.LENGTH_LONG
    toast.view = layout
    toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 150)
    toast.show()

    // Play success or error sound based on message
    when {
        message.startsWith("✅") -> SoundUtils.playSuccessSound(context)  // Success
        message.startsWith("❌") || message.startsWith("⚠") -> SoundUtils.playErrorSound(context)  // Error & Warning
    }
}



