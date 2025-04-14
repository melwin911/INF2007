package com.example.inf2007_mad_j1847.viewmodel

import com.example.inf2007_mad_j1847.model.ChatMessage
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for handling AI chatbot logic using Google's Gemini API.
 * Manages message history, loading states, and API error handling.
 */
class ChatbotViewModel : ViewModel() {

    // Internal state for chat history: List of com.example.inf2007_mad_j1847.model.ChatMessage (text + isUser flag)
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    // Loading state (e.g. while waiting for Gemini to respond)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Error message state (used for showing UI error feedback)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Function to sends a user message to chatbot and handle the response
    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            _messages.value = _messages.value + ChatMessage(userMessage, true) // Add user message
            _isLoading.value = true
            try {
                val response = getChatbotResponse(userMessage) // Call API to get response
                _messages.value = _messages.value + ChatMessage(
                    response,
                    false
                ) // Add bot's response
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Function to handle sending messages
    suspend fun sendMessage(userMessage: String, messages: List<Pair<String, Boolean>>, updateMessages: (List<Pair<String, Boolean>>) -> Unit) {
        updateMessages(messages + (userMessage to true) + ("Thinking..." to false))

        val response = getChatbotResponse(userMessage)
        updateMessages(messages + (userMessage to true) + (response to false))
    }

    // Function to call Gemini API
    suspend fun getChatbotResponse(userMessage: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = "AIzaSyCr-Z4CPSnJn-QV18sNk4MOgBZf1DqY-2Y"

                val model = GenerativeModel(
                    modelName = "gemini-1.5-pro",
                    apiKey = apiKey
                )

                // Instruction prompt to define chatbot behavior and context
                val systemInstructions = """
                You are an AI assistant for a mobile medical appointment app. Your goal is to help users:
                - Check into their appointments via GPS or Bluetooth.
                - Locate nearby hospitals using GPS.
                - Understand appointment schedules and receive reminders.
                - Assist elderly patients with simple, clear language.
                
                When answering questions, be direct, polite, and give step-by-step guidance.
                
                Example:
                - User: "How do I check in?"
                - AI: "To check in, ensure your Camera permission is enabled. Open the app and tap 'Check-In', then scan the QR code in the hospital to check in."
                
                - User: "How do I book appointments?"
                - AI: "Simply press the "Appointment Booking" tab in the home page and select your hospital, date, timeslot and service type to book an appointment at your preferred hospital!"
                
                - User: "What documents should I bring for my appointment?"
                - AI: "Please bring your ID and any medical reports if applicable."
                
                - User: "Can I cancel or reschedule my appointment?"
                - AI: "Yes, you can. Navigate to the 'Appointment Booking' tab in the application and select the 'Edit' button to reschedule your appointment or select the 'Delete' button to cancel your appointment. 
                
                - User: "This app is slow"
                - AI: "Try closing other apps or clearing the app cache from settings."
                
                - User: "Appointments are not loading"
                - AI: "Ensure you have a stable internet connection. If the problem persists, contact support."
               
                
                Always prioritize clear, user-friendly explanations.
            """.trimIndent()

                // Make the API call (append user message to system prompt)
                val response = model.generateContent("$systemInstructions\nUser: $userMessage")

                // Return the chatbot's response
                response.text ?: "I'm not sure how to respond to that!"
            } catch (e: Exception) {
                Log.e("com.example.inf2007_mad_j1847.view.ChatbotScreen", "Error fetching response", e)
                "Oops! Something went wrong: ${e.message}"  // Handle API failure gracefully
            }
        }
    }

}
