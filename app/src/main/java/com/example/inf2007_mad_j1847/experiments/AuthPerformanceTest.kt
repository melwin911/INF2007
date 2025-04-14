package com.example.inf2007_mad_j1847.experiments

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.system.measureTimeMillis

object AuthPerformanceTest {

    private val auth = FirebaseAuth.getInstance()
    private const val TAG = "AuthPerformanceTest"
    private const val TOTAL_TESTS = 100 // Number of authentication attempts

    /**
     * Runs authentication performance test
     */
    fun runTest() {
        CoroutineScope(Dispatchers.IO).launch {
            var signUpSuccess = 0
            var loginSuccess = 0
            var totalSignUpTime = 0L
            var totalLoginTime = 0L

            val totalTime = measureTimeMillis {
                repeat(TOTAL_TESTS) { index ->
                    val email = "testuser$index@example.com"
                    val password = "Test@12345"

                    // Measure sign-up time
                    val signUpTime = measureTimeMillis {
                        if (signUpUser(email, password)) signUpSuccess++
                    }
                    totalSignUpTime += signUpTime
                    Log.d(TAG, "Sign-Up #$index - Response Time: $signUpTime ms")

                    delay(200) // Prevent Firebase rate limits

                    // Measure login time
                    val loginTime = measureTimeMillis {
                        if (loginUser(email, password)) loginSuccess++
                    }
                    totalLoginTime += loginTime
                    Log.d(TAG, "Login #$index - Response Time: $loginTime ms")

                    delay(200)
                }
            }

            val avgSignUpTime = totalSignUpTime / TOTAL_TESTS
            val avgLoginTime = totalLoginTime / TOTAL_TESTS
            val signUpSuccessRate = (signUpSuccess.toDouble() / TOTAL_TESTS) * 100
            val loginSuccessRate = (loginSuccess.toDouble() / TOTAL_TESTS) * 100

            Log.d(TAG, "Total Test Time: $totalTime ms")
            Log.d(TAG, "Average Sign-Up Response Time: $avgSignUpTime ms")
            Log.d(TAG, "Sign-Up Success Rate: $signUpSuccessRate% ($signUpSuccess/$TOTAL_TESTS)")
            Log.d(TAG, "Average Login Response Time: $avgLoginTime ms")
            Log.d(TAG, "Login Success Rate: $loginSuccessRate% ($loginSuccess/$TOTAL_TESTS)")
        }
    }

    /**
     * Simulates user sign-up
     */
    private suspend fun signUpUser(email: String, password: String): Boolean {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Sign-Up Failed: ${e.message}")
            false
        }
    }

    /**
     * Simulates user login
     */
    private suspend fun loginUser(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Login Failed: ${e.message}")
            false
        }
    }
}
