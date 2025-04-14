package com.example.inf2007_mad_j1847.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignUpScreen(navController: NavController) {
    var name by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var isNameError by rememberSaveable { mutableStateOf(false) }
    var isUsernameError by rememberSaveable { mutableStateOf(false) }
    var isEmailError by rememberSaveable { mutableStateOf(false) }
    var isPasswordError by rememberSaveable { mutableStateOf(false) }
    var isConfirmPasswordError by rememberSaveable { mutableStateOf(false) }

    var errorMessages by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sign Up",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = name,
            onValueChange = {
                name = it
                isNameError = false
                errorMessages = emptyList()
            },
            label = { Text("Name") },
            isError = isNameError,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = username,
            onValueChange = {
                username = it
                isUsernameError = false
                errorMessages = emptyList()
            },
            label = { Text("Username") },
            isError = isUsernameError,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = email,
            onValueChange = {
                email = it
                isEmailError = false
                errorMessages = emptyList()
            },
            label = { Text("Email") },
            isError = isEmailError,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = password,
            onValueChange = {
                password = it
                isPasswordError = false
                errorMessages = emptyList()
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = isPasswordError,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                isConfirmPasswordError = false
                errorMessages = emptyList()
            },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = isConfirmPasswordError,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessages.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                errorMessages.forEach { message ->
                    Text(
                        text = "• $message",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Button(
            onClick = {
                errorMessages = validateSignUpFields(
                    name, username, email, password, confirmPassword,
                    onNameError = { isNameError = it },
                    onUsernameError = { isUsernameError = it },
                    onEmailError = { isEmailError = it },
                    onPasswordError = { isPasswordError = it },
                    onConfirmPasswordError = { isConfirmPasswordError = it }
                )
                if (errorMessages.isEmpty()) {
                    signUpWithFireBase(
                        name = name,
                        username = username,
                        email = email,
                        password = password
                    ) { error ->
                        if (error != null) {
                            errorMessages = listOf(error)
                            if (error.contains("Email")) isEmailError = true
                            if (error.contains("Username")) isUsernameError = true
                        } else {
                            navController.navigate("login_screen") {
                                popUpTo(0) // so that the users cannot go back to any other screen. like it clears the entire backstack
                            }
                        }
                    }
                }

            })
        {
            Text(text = "Sign Up")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text(text = "Back to Login")
        }
    }
}

fun validateSignUpFields(
    name: String,
    username: String,
    email: String,
    password: String,
    confirmPassword: String,
    onNameError: (Boolean) -> Unit,
    onUsernameError: (Boolean) -> Unit,
    onEmailError: (Boolean) -> Unit,
    onPasswordError: (Boolean) -> Unit,
    onConfirmPasswordError: (Boolean) -> Unit
): List<String> {
    val errors = mutableListOf<String>()

    if (name.isBlank()) {
        onNameError(true)
        errors.add("Name cannot be empty")
    }

    if (username.isBlank()) {
        onUsernameError(true)
        errors.add("Username is required")
    }

    if (!email.matches(Regex("""^[\w-\.]+@([\w-]+\.)+[\w-]{2,}$"""))) {
        onEmailError(true)
        errors.add("Invalid email format")
    }

    if (password.length < 6) {
        onPasswordError(true)
        errors.add("Password must be at least 6 characters long")
    }

    if (confirmPassword != password) {
        onConfirmPasswordError(true)
        errors.add("Passwords do not match")
    }

    return errors
}

fun signUpWithFireBase(
    name: String,
    username: String,
    email: String,
    password: String,
    onResult: (String?) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // Email already handled by Firebase Auth (It handles email uniqueness)

    // Check if username exists in Firestore
    db.collection("users")
        .whereEqualTo("username", username)
        .get()
        .addOnSuccessListener { usernameQuery ->
            if (!usernameQuery.isEmpty) {
                onResult("Username already exists")  // Error if username is taken
                return@addOnSuccessListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid

                        if (uid != null) {
                            db.collection("users").document(uid)
                                .set(
                                    hashMapOf(
                                        "name" to name,
                                        "username" to username,
                                        "email" to email
                                    )
                                )
                                .addOnSuccessListener {
                                    onResult(null)
                                }
                                .addOnFailureListener { e ->
                                    onResult(e.message)
                                }
                        } else {
                            onResult("Failed to create user")
                        }
                    } else {
                        onResult(task.exception?.message)
                    }
                }
                .addOnFailureListener { e ->
                    onResult(e.message)
                }
        }
        .addOnFailureListener { e ->
            onResult(e.message)
        }
}
