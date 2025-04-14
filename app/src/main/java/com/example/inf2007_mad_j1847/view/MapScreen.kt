package com.example.inf2007_mad_j1847.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current

    val mapView = remember { MapView(context) }

    // State to track location permission
    var hasLocationPermission by remember {
        mutableStateOf(isLocationPermissionGranted(context))
    }

    // Permission launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            hasLocationPermission = true
        } else {
            showToast(context, "Location permission is required to show your position.")
        }
    }

    // Always check for permission and request if needed
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    DisposableEffect(mapView) {
        mapView.onCreate(Bundle())
        mapView.onStart()
        mapView.onResume()

        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hospital Locator") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            AndroidView(modifier = Modifier.fillMaxSize(), factory = { mapView })
        }
        // Initialize map when it's ready
        LaunchedEffect(mapView) {
            mapView.getMapAsync { googleMap ->
                setupMap(googleMap, context, hasLocationPermission) {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }
    }
}

// Function to configure the map
@SuppressLint("MissingPermission")
fun setupMap(
    googleMap: GoogleMap,
    context: Context,
    hasLocationPermission: Boolean,
    requestPermissionLauncher: () -> Unit
) {
    val singapore = LatLng(1.3521, 103.8198)

    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(singapore, 12f))

    val hospitals = listOf(
        Pair(LatLng(1.395605783403444, 103.89351144022423), "Sengkang Community Hospital"),
        Pair(LatLng(1.2866507033394214, 103.83508914649981), "Singapore General Hospital"),
        Pair(LatLng(1.3223968883538861, 103.84406025371985), "Mount Elizabeth Novena Hospital"),
        Pair(LatLng(1.2939714015494668, 103.78315245032486), "National University Hospital"),
        Pair(LatLng(1.4245760621006895, 103.83864434022425), "Khoo Teck Puat Hospital"),
        Pair(LatLng(1.3403631130642695, 103.94948426537098), "Changi General Hospital"),
    )

    hospitals.forEach { (location, name) ->
        googleMap.addMarker(MarkerOptions().position(location).title(name))
    }

    if (hasLocationPermission) {
        googleMap.isMyLocationEnabled = true

        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val userLocation = LatLng(it.latitude, it.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
            }
        }
    } else {
        // If no permission, ask again
        requestPermissionLauncher()
    }

    googleMap.setOnMapLoadedCallback {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(singapore, 12f))
    }
}

// Function to check if location permission is granted
fun isLocationPermissionGranted(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

// Function to show a toast message
fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}