package com.example.inf2007_mad_j1847.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inf2007_mad_j1847.model.User
import com.example.inf2007_mad_j1847.viewmodel.ProfileViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel = viewModel()) {
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showUpdateDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showPasswordUpdateDialog by remember { mutableStateOf(false) }
    var updatedUser by remember { mutableStateOf<User?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val accountDeleted by viewModel.accountDeleted.collectAsState()
    LaunchedEffect(accountDeleted) {
        if (accountDeleted) {
            navController.navigate("auth_graph") {
                popUpTo("login_screen") { inclusive = true }  // Remove profile from back stack
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (errorMessage != null) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            } else {
                user?.let {
                    Text(text = "Name: ${it.name}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Username: ${it.username}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Email: ${it.email}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { showPasswordUpdateDialog = true }) {
                        Text("Update Password")
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(onClick = { showUpdateDialog = true }) {
                        Text("Update Profile Details")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete Account")
                    }
                }
            }
        }
    }

    // Show Delete Account Dialog
    if (showDeleteDialog) {
        DeleteAccountDialog(
            onClose = { showDeleteDialog = false },
            onDelete = { password ->
                viewModel.deleteUserAccount(password)
                showDeleteDialog = false
            }
        )
    }

    // Show Update Profile Dialog
    if (showUpdateDialog && user != null) {
        UpdateUserDialog(
            currentUser = user!!,
            onClose = { showUpdateDialog = false },
            onUpdate = { newUser ->
                updatedUser = newUser
                showUpdateDialog = false
                showPasswordDialog = true
            }
        )
    }

    // Show Password Confirmation Dialog for profile updates
    if (showPasswordDialog) {
        ConfirmPasswordDialog(
            onClose = { showPasswordDialog = false },
            onConfirm = { password ->
                updatedUser?.let {
                    viewModel.updateUser(it, password)
                }
                showPasswordDialog = false
            }
        )
    }

    // Show Password Update Dialog
    if (showPasswordUpdateDialog) {
        UpdatePasswordDialog(
            onClose = { showPasswordUpdateDialog = false },
            onUpdatePassword = { currentPassword, newPassword ->
                viewModel.updatePassword(currentPassword, newPassword)
                showPasswordUpdateDialog = false
            }
        )
    }
}

@Composable
fun DeleteAccountDialog(
    onClose: () -> Unit,
    onDelete: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmationChecked by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Delete Account") },
        text = {
            Column {
                Text("This action is irreversible. Your account and all associated data will be permanently deleted.")
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Enter Current Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = confirmationChecked,
                        onCheckedChange = { confirmationChecked = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("I understand and wish to proceed")
                }
                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Please enter a valid password and confirm.", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (password.isNotBlank() && confirmationChecked) {
                        onDelete(password)
                    } else {
                        showError = true
                    }
                },
                enabled = confirmationChecked && password.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            Button(onClick = onClose) { Text("Cancel") }
        }
    )
}

@Composable
fun UpdatePasswordDialog(
    onClose: () -> Unit,
    onUpdatePassword: (String, String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Update Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPassword != confirmPassword) {
                        errorMessage = "Passwords do not match."
                    } else if (newPassword.length < 6) {
                        errorMessage = "Password must be at least 6 characters long."
                    } else {
                        onUpdatePassword(currentPassword, newPassword)
                    }
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = { Button(onClick = onClose) { Text("Cancel") } }
    )
}


@Composable
fun UpdateUserDialog(
    currentUser: User,
    onClose: () -> Unit,
    onUpdate: (User) -> Unit
) {
    var newName by remember { mutableStateOf(currentUser.name) }
    var newUsername by remember { mutableStateOf(currentUser.username) }
    var newEmail by remember { mutableStateOf(currentUser.email) }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Update Profile") },
        text = {
            Column {
                OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Name") })
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = newUsername, onValueChange = { newUsername = it }, label = { Text("Username") })
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = newEmail, onValueChange = { newEmail = it }, label = { Text("Email") })
            }
        },
        confirmButton = {
            Button(
                onClick = { onUpdate(User(currentUser.id, newName, newUsername, newEmail)) },
                enabled = newName != currentUser.name || newUsername != currentUser.username || newEmail != currentUser.email
            ) {
                Text("Update")
            }
        },
        dismissButton = { Button(onClick = onClose) { Text("Cancel") } }
    )
}


@Composable
fun ConfirmPasswordDialog(
    onClose: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Confirm Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Enter Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                if (showError) {
                    Text("Incorrect password. Please try again.", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (password.isNotBlank()) {
                        onConfirm(password)
                    } else {
                        showError = true
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = { Button(onClick = onClose) { Text("Cancel") } }
    )
}
