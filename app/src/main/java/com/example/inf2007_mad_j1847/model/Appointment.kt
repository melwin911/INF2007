package com.example.inf2007_mad_j1847.model

import com.google.firebase.Timestamp

/**
 * Data class representing a medical appointment.
 * This structure is used for storing and retrieving appointment data from Firebase.
 */

data class Appointment(
    val id: String = "",    // Unique ID for the appointment
    val date: Timestamp? = null,  // To ensure it's stored as Timestamp to align with firebase
    val doctor: String = "",
    val location: String = "",
    val type: String = "",
    val username: String = "",
    val status: String = "",
    val completion_time: Timestamp? = null,  // To ensure it's stored as nullable Timestamp
    val uid: String = ""    // Firebase UID of the user (used for user-specific data access)
)