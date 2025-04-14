package com.example.inf2007_mad_j1847.experiments

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis
import kotlin.random.Random

object QRCheckInTest {

    private val firestore = FirebaseFirestore.getInstance()
    private const val TAG = "QRCheckInTest"
    private const val TOTAL_SCANS = 100

    // Dynamically generate QR codes (instead of predefined ones)
    private val generatedQRCodes = List(TOTAL_SCANS) { "hospital_checkin_${Random.nextInt(1000, 9999)}" }

    private val fakeGPSLocations = listOf(
        Pair(1.290270, 103.851959),  // Fake location 1 (Singapore landmark)
        Pair(1.352083, 103.819836),  // Fake location 2
        Pair(37.7749, -122.4194),    // Completely wrong location (San Francisco)
    )
    private val actualHospitalLocation = Pair(1.3000, 103.8000) // Simulated hospital location

    /**
     * Runs the QR check-in reliability test
     */
    fun runTest() {
        CoroutineScope(Dispatchers.IO).launch {
            var successCount = 0
            var failedScans = 0
            var gpsFailures = 0
            var totalResponseTime = 0L

            val totalTime = measureTimeMillis {
                generatedQRCodes.forEachIndexed { index, qrCode ->
                    val responseTime = measureTimeMillis {
                        val isScanSuccessful = scanQRCode(qrCode)
                        val isGPSValid = validateGPS(actualHospitalLocation)

                        if (isScanSuccessful && isGPSValid) {
                            successCount++
                        } else {
                            if (!isScanSuccessful) failedScans++
                            if (!isGPSValid) gpsFailures++
                        }
                    }
                    totalResponseTime += responseTime
                    Log.d(TAG, "QR Scan #$index - Response Time: $responseTime ms")
                }
            }

            val averageScanTime = totalResponseTime / TOTAL_SCANS
            val successRate = (successCount.toDouble() / TOTAL_SCANS) * 100
            val gpsFailureRate = (gpsFailures.toDouble() / TOTAL_SCANS) * 100

            Log.d(TAG, "Total Test Time: $totalTime ms")
            Log.d(TAG, "Average QR Scan Response Time: $averageScanTime ms")
            Log.d(TAG, "QR Scan Success Rate: $successRate% ($successCount/$TOTAL_SCANS)")
            Log.d(TAG, "GPS Validation Failure Rate: $gpsFailureRate% ($gpsFailures/$TOTAL_SCANS)")
        }
    }

    /**
     * Simulates QR code scanning
     */
    private suspend fun scanQRCode(qrData: String): Boolean {
        return withContext(Dispatchers.IO) {
            delay((300..1000).random().toLong()) // Simulate QR scan processing time (300ms - 1s)

            val isMatch = generatedQRCodes.any { validQR ->
                qrData.contains(validQR, ignoreCase = true) // Allow minor variations in QR content
            }

            if (!isMatch) {
                Log.e(TAG, "QR Scan Failed! Scanned QR: $qrData")
            }

            return@withContext isMatch
        }
    }

    /**
     * Simulates GPS validation for check-in
     */
    private fun validateGPS(actualLocation: Pair<Double, Double>): Boolean {
        // Simulate realistic GPS readings (90% of the time, user is within the correct location)
        val isAccurate = (1..100).random() > 10 // 90% accuracy
        val fakeLocation = if (isAccurate) actualLocation else fakeGPSLocations.random()

        val distance = calculateDistance(actualLocation, fakeLocation)
        val isValid = distance < 0.5 // Allow check-in only if within 500m

        if (!isValid) {
            Log.e(TAG, "GPS Validation Failed! Fake Location: $fakeLocation, Distance: $distance km")
        }

        return isValid
    }

    /**
     * Calculates the distance between two GPS coordinates (approximate)
     */
    private fun calculateDistance(loc1: Pair<Double, Double>, loc2: Pair<Double, Double>): Double {
        val latDiff = loc1.first - loc2.first
        val lonDiff = loc1.second - loc2.second
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff) * 111 // Approx conversion to km
    }
}
