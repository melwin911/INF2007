package com.example.inf2007_mad_j1847.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(navController: NavController) {
    var emailUsername by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        TextField(
            value = emailUsername,
            onValueChange = { emailUsername = it },
            label = { Text("Email/Username") },
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            enabled = emailUsername.isNotEmpty() && password.isNotEmpty() && !isLoading,
            onClick = {
                errorMessage = ""
                isLoading = true

                signInWithFireBase(emailUsername, password) { result ->
                    isLoading = false

                    if (result == null) {
                        navController.navigate("home_screen"){
                            popUpTo("login_screen") { inclusive = true }
                        }
                    } else {
                        errorMessage = result
                        password = ""
                    }
                }

            },
            modifier = Modifier.padding(bottom = 8.dp)
                .testTag("loginButton")
        ) {
            Text("Login")
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp).testTag("errorMessage")
            )
        }


        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { navController.navigate("signup_screen") }) {
            Text(text = "Go to Signup")
        }
    }
}

fun signInWithFireBase(emailUsername: String, password: String, onResult: (String?) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    if (emailUsername.contains("@")) {
        auth.signInWithEmailAndPassword(emailUsername, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(null)
                } else {
                    onResult("Invalid email/username or password. Please try again.")
                }
            }
    } else {
        db.collection("users")
            .whereEqualTo("username", emailUsername)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val email = documents.documents[0].getString("email")
                    if (!email.isNullOrEmpty()) {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    onResult(null)
                                } else {
                                    onResult("Invalid email/username or password. Please try again.")
                                }
                            }
                    } else {
                        onResult("Invalid email/username or password. Please try again.")
                    }
                } else {
                    onResult("Invalid email/username or password. Please try again.")
                }
            }
            .addOnFailureListener {
                onResult("An error occurred. Please try again later.")
            }
    }
}