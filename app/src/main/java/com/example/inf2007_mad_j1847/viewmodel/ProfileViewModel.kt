package com.example.inf2007_mad_j1847.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inf2007_mad_j1847.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> get() = _loading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _accountDeleted = MutableStateFlow(false)
    val accountDeleted: StateFlow<Boolean> get() = _accountDeleted

    init {
        fetchUser()
    }

    private fun fetchUser() {
        viewModelScope.launch {
            _loading.value = true
            val userId = auth.currentUser?.uid ?: return@launch

            try {
                val snapshot = db.collection("users").document(userId).get().await()
                val fetchedUser = snapshot.toObject(User::class.java)
                Log.d("ProfileViewModel", "User fetched: $fetchedUser") // Debugging Log

                _user.value = fetchedUser
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load profile: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateUser(newUser: User, password: String) {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _errorMessage.value = "User not authenticated."
                return@launch
            }

            val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)

            try {
                // Re-authenticate user with password
                currentUser.reauthenticate(credential).await()

                // If re-authentication is successful, update the user details
                db.collection("users").document(currentUser.uid).set(newUser).await()
                _user.value = newUser  // Update UI after successful update
            } catch (e: Exception) {
                _errorMessage.value = "Incorrect password or update failed: ${e.message}"
            }
        }
    }

    fun updatePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _errorMessage.value = "User not authenticated."
                return@launch
            }

            val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)

            try {
                // Re-authenticate user with the current password
                currentUser.reauthenticate(credential).await()

                // If re-authentication is successful, update the password
                currentUser.updatePassword(newPassword).await()
            } catch (e: Exception) {
                _errorMessage.value = "Password update failed: ${e.message}"
            }
        }
    }

    fun deleteUserAccount(password: String) {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _errorMessage.value = "User not authenticated."
                return@launch
            }

            val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)

            try {
                // Re-authenticate user with the provided password
                currentUser.reauthenticate(credential).await()

                // Delete user data from Firestore
                db.collection("users").document(currentUser.uid).delete().await()

                // Delete user account from Firebase Authentication
                currentUser.delete().await()

                // Clear local user state
                _user.value = null
                _accountDeleted.value = true  // Notify UI to redirect
            } catch (e: Exception) {
                _errorMessage.value = "Account deletion failed: ${e.message}"
            }
        }
    }
}
