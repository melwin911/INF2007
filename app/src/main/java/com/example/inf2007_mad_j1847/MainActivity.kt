package com.example.inf2007_mad_j1847

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.inf2007_mad_j1847.ui.theme.INF2007_MAD_J1847Theme
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.example.inf2007_mad_j1847.experiments.AppointmentBookingTest
import com.example.inf2007_mad_j1847.experiments.AuthPerformanceTest
import com.example.inf2007_mad_j1847.experiments.ChatbotPerformanceTest
import com.example.inf2007_mad_j1847.experiments.QRCheckInTest


class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // EXPERIMENTS
        // Run appointment booking performance test
//       AppointmentBookingTest.runTest()

        // Run chatbot performance test
//       ChatbotPerformanceTest.runTest()

        // Run QR check-in test
//        QRCheckInTest.runTest()

        // Run authentication performance test
//        AuthPerformanceTest.runTest()

        var nameTest by mutableStateOf("Android") // Default name

        // Initialize Firebase Realtime Database
        val database = Firebase.database("https://inf2007-smartapptbookingapp-default-rtdb.asia-southeast1.firebasedatabase.app")
        val myRef = database.getReference("message")
        myRef.setValue("Hello, World!") // Write to the database

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                nameTest = dataSnapshot.getValue<String>().orEmpty()
                Log.d(TAG, "Value is: $nameTest")
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        setContent {
            INF2007_MAD_J1847Theme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.SpaceBetween, // Pushes content apart
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Navigation Graph
                        NavGraph(navController = navController)

                        // Spacer to push text to bottom
                        Spacer(modifier = Modifier.weight(1f))

                        // Greeting text at the bottom
                        Greeting(name = nameTest)

                        Spacer(modifier = Modifier.height(16.dp)) // Optional padding at bottom
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
