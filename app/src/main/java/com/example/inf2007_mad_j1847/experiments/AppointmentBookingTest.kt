package com.example.inf2007_mad_j1847.experiments

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.system.measureTimeMillis

object AppointmentBookingTest {

    private val firestore = FirebaseFirestore.getInstance()
    private const val TAG = "AppointmentBookingTest"
    private const val TOTAL_BOOKINGS = 1000
    private const val BATCH_SIZE = 50  // Number of bookings per batch
    private const val DELAY_BETWEEN_BATCHES = 500L  // 500ms delay between batch writes

    /**
     * Runs the appointment booking test for 1000 simulated users.
     */
    fun runTest() {
        CoroutineScope(Dispatchers.IO).launch {
            var successCount = 0
            val totalTime = measureTimeMillis {
                for (i in 0 until TOTAL_BOOKINGS step BATCH_SIZE) {
                    val batch = firestore.batch()

                    for (j in i until minOf(i + BATCH_SIZE, TOTAL_BOOKINGS)) {
                        val docRef = firestore.collection("appointments").document()
                        val appointmentData = hashMapOf(
                            "userId" to "testUser$j",
                            "doctorId" to "doctor123",
                            "hospital" to "Test Hospital",
                            "timeSlot" to "10:00 AM",
                            "status" to "Pending"
                        )
                        batch.set(docRef, appointmentData)
                    }

                    try {
                        batch.commit().await() // Commit the batch
                        successCount += BATCH_SIZE
                        Log.d(TAG, "Batch from $i to ${i + BATCH_SIZE} committed successfully.")
                    } catch (e: Exception) {
                        Log.e(TAG, "Batch commit failed: ${e.message}")
                    }

                    delay(DELAY_BETWEEN_BATCHES) // Prevent hitting Firestore rate limits
                }
            }

            Log.d(TAG, "Total time for $TOTAL_BOOKINGS bookings: $totalTime ms")
            Log.d(TAG, "Success Rate: $successCount/$TOTAL_BOOKINGS")
        }
    }
}
