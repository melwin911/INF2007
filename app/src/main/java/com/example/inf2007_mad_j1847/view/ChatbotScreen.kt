package com.example.inf2007_mad_j1847.view

import com.example.inf2007_mad_j1847.viewmodel.ChatbotViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * The interactive AI assistant interface within the app.
 * Users can type health-related questions or app-related queries, and receive real-time responses powered by Google's Gemini AI.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(navController: NavController, viewModel: ChatbotViewModel = viewModel()) {
    // State observation from ViewModel
    val messages by viewModel.messages.collectAsState()    // Chat history
    val isLoading by viewModel.isLoading.collectAsState()   // Show loading spinner if waiting for chatbot
    val errorMessage by viewModel.errorMessage.collectAsState()   // Show error if chatbot fails
    var userInput by remember { mutableStateOf("") }    // Tracks text input by user

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Chatbot") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Displaying Chat History (latest messages at bottom)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = true // Ensures newest message appears at bottom
            ) {
                // Display each message bubble
                items(messages.reversed()) { (message, isUser) ->
                    ChatBubble(message, isUser)
                }
            }

            // Show spinner while loading response
            if (isLoading) {
                CircularProgressIndicator()
            }

            // Show error if any
            if (errorMessage != null) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Input Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Text input field
                BasicTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (userInput.isNotBlank()) {
                                viewModel.sendMessage(userInput) // Send message to ViewModel
                                userInput = ""
                            }
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Placeholder text
                            if (userInput.isEmpty()) {
                                Text(
                                    text = "Type a message...",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send button
                Button(
                    onClick = {
                        if (userInput.isNotBlank()) {
                            viewModel.sendMessage(userInput) // Send message to ViewModel
                            userInput = ""
                        }
                    }
                ) {
                    Text("Send")
                }
            }
        }
    }
}

// Chat Bubble Composable
@Composable
fun ChatBubble(text: String, isUser: Boolean) {
    val backgroundColor = if (isUser) Color(0xFF4CAF50) else Color(0xFF2196F3)
    val textColor = Color.White
    val alignment = if (isUser) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = alignment
    ) {
        Box(
            modifier = Modifier
                .background(backgroundColor, shape = RoundedCornerShape(12.dp))
                .padding(12.dp)
                .widthIn(min = 50.dp, max = 280.dp)
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 16.sp,
                textAlign = TextAlign.Start
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
}
