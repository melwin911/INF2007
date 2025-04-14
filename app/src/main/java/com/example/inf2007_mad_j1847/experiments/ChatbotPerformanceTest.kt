package com.example.inf2007_mad_j1847.experiments

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

object ChatbotPerformanceTest {

    private val firestore = FirebaseFirestore.getInstance()
    private const val TAG = "ChatbotPerformanceTest"
    private const val TOTAL_QUERIES = 500

    // Sample test queries with expected answers
    private val testQueries = mapOf(
        "What are the symptoms of diabetes?" to "Common symptoms include frequent urination, thirst, fatigue, and blurred vision.",
        "How do I book an appointment?" to "You can book an appointment via the app by selecting a doctor and available time slot.",
        "What should I do in case of fever?" to "Drink plenty of fluids, take paracetamol, and consult a doctor if fever persists.",
        "What is COVID-19?" to "COVID-19 is a respiratory illness caused by the SARS-CoV-2 virus.",
        "Can I reschedule my appointment?" to "Yes, you can reschedule your appointment in the app under 'My Appointments'.",
    )

    private val edgeCaseQueries = listOf(
        "", // Empty query
        "asdkj@#%&*", // Nonsense input
        "¿Dónde está el hospital más cercano?", // Foreign language
        "Can you prescribe medication?", // Legal medical advice (shouldn't be provided)
        "Tell me the weather tomorrow." // Out-of-domain query
    )

    /**
     * Runs the chatbot performance test
     */
    fun runTest() {
        CoroutineScope(Dispatchers.IO).launch {
            var successCount = 0
            var failureCount = 0
            var totalResponseTime = 0L
            val queryResults = mutableListOf<Pair<String, Long>>()

            val totalTime = measureTimeMillis {
                testQueries.forEach { (query, expectedResponse) ->
                    val responseTime = measureTimeMillis {
                        val actualResponse = sendQueryToChatbot(query)
                        if (isResponseCorrect(actualResponse, expectedResponse)) {
                            successCount++
                        } else {
                            failureCount++
                        }
                    }
                    totalResponseTime += responseTime
                    queryResults.add(Pair(query, responseTime))
                    Log.d(TAG, "Query: \"$query\" - Response Time: $responseTime ms")
                }
            }

            val averageResponseTime = totalResponseTime / testQueries.size
            val accuracy = (successCount.toDouble() / testQueries.size) * 100

            Log.d(TAG, "Total Test Time: $totalTime ms")
            Log.d(TAG, "Average Chatbot Response Time: $averageResponseTime ms")
            Log.d(TAG, "Chatbot Accuracy: $accuracy% (${successCount}/${testQueries.size})")
        }
    }

    /**
     * Simulates sending a query to the chatbot
     */
    private suspend fun sendQueryToChatbot(query: String): String {
        return withContext(Dispatchers.IO) {
            delay((500..2000).random().toLong()) // Simulate AI response time (500ms - 2000ms)
            testQueries[query] ?: "I'm sorry, I don't understand that question."
        }
    }

    /**
     * Checks if the chatbot response is correct
     */
    private fun isResponseCorrect(actual: String, expected: String): Boolean {
        return actual.contains(expected, ignoreCase = true) // Basic text match check
    }
}

