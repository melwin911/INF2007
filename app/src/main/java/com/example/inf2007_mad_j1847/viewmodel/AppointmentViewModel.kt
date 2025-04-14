package com.example.inf2007_mad_j1847.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inf2007_mad_j1847.model.Appointment
import com.example.inf2007_mad_j1847.utils.LocationData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

/**
 * ViewModel for managing appointment-related data and operations.
 * Handles communication with Firebase and UI state management.
 */
class AppointmentViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Public appointment list
    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    // Static lists for dropdowns
    val hospitals = listOf("Sengkang Community Hospital", "Singapore General Hospital", "National University Hospital", "Khoo Teck Puat Hospital", "Mount Elizabeth Novena Hospital", "Changi General Hospital")
    val services = listOf("General Checkup", "Dental Checkup", "Cardiology", "Dermatology", "Orthopedics")
    val doctors = listOf("Dr. Smith", "Dr. Lee", "Dr. Alex")

    // Single appointment (used for detail/edit appointment)
    private val _selectedAppointment = MutableStateFlow<Appointment?>(null)
    val selectedAppointment: StateFlow<Appointment?> = _selectedAppointment

    // UI-related states
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    // Fetch All Appointments for Logged-In User
    fun fetchAppointments() {
        val userId = auth.currentUser?.uid ?: return

        _loading.value = true
        viewModelScope.launch {
            try {
                val result = db.collection("appointments")
                    .whereEqualTo("uid", userId)
                    .get()
                    .await()

                val fetchedAppointments = result.documents.mapNotNull { appt ->
                    val id = appt.id
                    val date = appt.getTimestamp("date")
                    val doctor = appt.getString("doctor") ?: ""
                    val location = appt.getString("location") ?: ""
                    val type = appt.getString("type") ?: ""
                    val username = appt.getString("username") ?: ""
                    val status = appt.getString("status") ?: ""
                    val completion_time = appt.getTimestamp("completion_time")
                    val uid = appt.getString("uid") ?: ""

                    Appointment(id, date, doctor, location, type, username, status, completion_time, uid)
                }

                _appointments.value = fetchedAppointments
                _loading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                _loading.value = false
                Log.e("AppointmentViewModel", "Error fetching appointments", e)
            }
        }
    }

    // Book New Appointment
    fun bookAppointment(appointment: Appointment, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val docRef = db.collection("appointments").add(appointment).await()
                db.collection("appointments").document(docRef.id).update("id", docRef.id).await()
                _loading.value = false
                setSuccessMessage("✅ Appointment Booked!")
                onSuccess()
            } catch (e: Exception) {
                _loading.value = false
                onFailure("Error: ${e.message}")
            }
        }
    }

    // Get Appointment Details By ID
    fun getAppointmentById(appointmentId: String) {
        _loading.value = true
        _selectedAppointment.value = null
        viewModelScope.launch {
            try {
                val document = db.collection("appointments").document(appointmentId).get().await()
                val appointment = document.toObject(Appointment::class.java)
                fetchAppointments()
                _selectedAppointment.value = appointment
                _loading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                _loading.value = false
                Log.e("AppointmentViewModel", "Error fetching appointment", e)
            }
        }
    }

    // Update Appointment
    fun updateAppointment(appointmentId: String, updatedData: Map<String, Any>, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        _loading.value = true
        Log.d("Firestore Update", "Updating appointment: $appointmentId with data: $updatedData")
        viewModelScope.launch {
            try {
                db.collection("appointments").document(appointmentId).update(updatedData).await()
                fetchAppointments() // Refresh the list after update
                _loading.value = false
                setSuccessMessage("✅ Appointment Updated!")
                onSuccess()
            } catch (e: Exception) {
                _loading.value = false
                Log.e("Firestore Update", "Error updating appointment: ${e.message}", e)
                onFailure("Error: ${e.message}")
            }
        }
    }

    // Delete Appointment
    fun deleteAppointment(appointmentId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                db.collection("appointments").document(appointmentId).delete().await()
                onSuccess()
            } catch (e: Exception) {
                Log.e("AppointmentViewModel", "Error deleting appointment", e)
                onFailure("Error: ${e.message}")
            }
        }
    }

    //  Message Helper Functions
    fun setSuccessMessage(message: String) {
        _successMessage.value = message
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    // Function to get today's appointments at a specific hospital for check-in
    fun getTodayAppointmentsAtLocation(
        hospitalName: String,
        onSuccess: (List<Appointment>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        _loading.value = true

        viewModelScope.launch {
            try {
                // get the current date (start and end of the day)
                val calendar = Calendar.getInstance()
                val startOfDay = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.time

                val endOfDay = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.time

                // find user's appointment for today at this location
                val result = db.collection("appointments")
                    .whereEqualTo("uid", userId)
                    .whereEqualTo("location", hospitalName)
                    .whereEqualTo("status", "upcoming")
                    .whereGreaterThanOrEqualTo("date", Timestamp(startOfDay))
                    .whereLessThanOrEqualTo("date", Timestamp(endOfDay))
                    .get()
                    .await()

                // mapping the stored value in Firebase
                val todayAppointments = result.documents.mapNotNull { doc ->
                    val id = doc.id
                    val date = doc.getTimestamp("date")
                    val doctor = doc.getString("doctor") ?: ""
                    val location = doc.getString("location") ?: ""
                    val type = doc.getString("type") ?: ""
                    val username = doc.getString("username") ?: ""
                    val status = doc.getString("status") ?: ""
                    val completion_time = doc.getTimestamp("completion_time")
                    val uid = doc.getString("uid") ?: ""

                    Appointment(id, date, doctor, location, type, username, status, completion_time, uid)
                }

                _loading.value = false
                onSuccess(todayAppointments)
            } catch (e: Exception) {
                _loading.value = false
                _errorMessage.value = "Error: ${e.message}"
                onFailure("Failed to load appointments: ${e.message}")
                Log.e("AppointmentViewModel", "Error fetching today's appointments", e)
            }
        }
    }

    // function to check in for appointment
    fun checkInAppointment(
        appointmentId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        _loading.value = true

        viewModelScope.launch {
            try {
                // get the appointment
                val appointmentDoc = db.collection("appointments").document(appointmentId).get().await()
                if (!appointmentDoc.exists()) {
                    _loading.value = false
                    onFailure("Appointment not found")
                    return@launch
                }

                // get appointment time
                val appointmentTime = appointmentDoc.getTimestamp("date")
                if (appointmentTime == null) {
                    _loading.value = false
                    onFailure("Invalid appointment data")
                    return@launch
                }

                // get current time and check timing
                val currentTime = Timestamp.now()

                // calculate time difference in minutes
                val diffInMillis = appointmentTime.toDate().time - currentTime.toDate().time
                val diffInMinutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(diffInMillis)

                // determine status based on timing
                val newStatus = when {
                    diffInMinutes < -5 -> "missed" // > 5 minutes late
                    else -> "completed" // on time or early
                }

                // update the appointment
                db.collection("appointments").document(appointmentId)
                    .update(
                        mapOf(
                            "status" to newStatus,
                            "completion_time" to currentTime
                        )
                    )
                    .await()

                // refresh appointments list
                fetchAppointments()

                _loading.value = false
                setSuccessMessage("✅ Check-in successful!")
                onSuccess()
            } catch (e: Exception) {
                _loading.value = false
                onFailure("Check-in failed: ${e.message}")
                Log.e("AppointmentViewModel", "Error checking in", e)
            }
        }
    }

    // function to verify if user is at the hospital
    fun isUserAtHospital(hospitalName: String, userLat: Double, userLng: Double): Boolean {
        return LocationData.isUserAtHospital(hospitalName, userLat, userLng)
    }

    fun batchCheckInAppointments(
        appointmentIds: List<String>,
        onProgress: (current: Int, total: Int) -> Unit,
        onComplete: (successful: Int, failed: Int) -> Unit
    ) {
        if (appointmentIds.isEmpty()) {
            onComplete(0, 0)
            return
        }

        _loading.value = true

        viewModelScope.launch {
            var successCount = 0
            var failureCount = 0

            appointmentIds.forEachIndexed { index, appointmentId ->
                try {
                    // get the appointment
                    val appointmentDoc = db.collection("appointments").document(appointmentId).get().await()
                    if (!appointmentDoc.exists()) {
                        failureCount++
                        onProgress(index + 1, appointmentIds.size)
                        return@forEachIndexed
                    }

                    // get appointment time
                    val appointmentTime = appointmentDoc.getTimestamp("date")
                    if (appointmentTime == null) {
                        failureCount++
                        onProgress(index + 1, appointmentIds.size)
                        return@forEachIndexed
                    }

                    // get current time and check timing
                    val currentTime = Timestamp.now()

                    // calculate time difference in minutes
                    val diffInMillis = appointmentTime.toDate().time - currentTime.toDate().time
                    val diffInMinutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(diffInMillis)

                    // determine status based on timing
                    val newStatus = when {
                        diffInMinutes < -5 -> "missed" // More than 5 minutes late
                        else -> "completed"  // On time
                    }

                    // update the appointment
                    db.collection("appointments").document(appointmentId)
                        .update(
                            mapOf(
                                "status" to newStatus,
                                "completion_time" to currentTime
                            )
                        )
                        .await()

                    successCount++
                } catch (e: Exception) {
                    Log.e("AppointmentViewModel", "Error checking in appointment $appointmentId", e)
                    failureCount++
                } finally {
                    onProgress(index + 1, appointmentIds.size)
                }
            }

            // refresh appointments list
            fetchAppointments()

            _loading.value = false
            onComplete(successCount, failureCount)
        }
    }
}
