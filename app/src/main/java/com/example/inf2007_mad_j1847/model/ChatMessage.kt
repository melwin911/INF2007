package com.example.inf2007_mad_j1847.model

/**
 * Data class representing a single message in the chatbot conversation.
 *
 * @param message The content of the chat message.
 * @param isUser A flag indicating whether the message was sent by the user (true) or the bot (false).
 */

data class ChatMessage(
    val message: String,
    val isUser: Boolean    // True if sent by the user; false if sent by the chatbot
)