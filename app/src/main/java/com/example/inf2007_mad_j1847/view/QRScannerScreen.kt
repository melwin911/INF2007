package com.example.inf2007_mad_j1847.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inf2007_mad_j1847.utils.PortraitCaptureActivity
import com.example.inf2007_mad_j1847.viewmodel.AppointmentViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    navController: NavController,
    viewModel: AppointmentViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // state for permissions and location
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isGettingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }

    // permission requesters
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        hasLocationPermission = locationGranted
    }

    // qr scanner launcher
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val hospitalName = result.contents

            if (!hasLocationPermission) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Location permission required for check-in")
                }
                return@rememberLauncherForActivityResult
            }

            isGettingLocation = true

            // get user location and verify presence at hospital
            getUserLocation(
                context = context,
                onLocationReceived = { location ->
                    isGettingLocation = false
                    val isAtHospital = viewModel.isUserAtHospital(
                        hospitalName = hospitalName,
                        userLat = location.latitude,
                        userLng = location.longitude
                    )

                    if (isAtHospital) {
                        // if user is at the hospital - proceed to selection screen
                        navController.navigate("appointment_selection/$hospitalName")
                    } else {
                        // if user is not at the correct location
                        coroutineScope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "You don't appear to be at $hospitalName",
                                actionLabel = "Override",
                                duration = SnackbarDuration.Long
                            )

                            // allow override for testing or GPS issues
                            if (result == SnackbarResult.ActionPerformed) {
                                navController.navigate("appointment_selection/$hospitalName")
                            }
                        }
                    }
                },
                onLocationError = { error ->
                    isGettingLocation = false
                    locationError = error
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Location error: $error")
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Check-In Scanner") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isGettingLocation) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Getting your location...")
            } else {
                // instruction text
                Text(
                    text = "Scan the QR code at your hospital's check-in station",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // location permission status
                if (!hasLocationPermission) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Location permission is required for check-in verification",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    Button(
                        onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        Text("Grant Location Permission")
                    }
                }

                // camera permission status
                if (!hasCameraPermission) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Camera",
                                tint = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Camera permission is required to scan QR codes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    Button(
                        onClick = {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        Text("Grant Camera Permission")
                    }
                }

                // scan button
                Button(
                    onClick = {
                        if (!hasCameraPermission) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            return@Button
                        }

                        if (!hasLocationPermission) {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                            return@Button
                        }

                        // launch QR scanner
                        val options = ScanOptions()
                            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                            .setPrompt("Scan the hospital check-in QR code")
                            .setBeepEnabled(true)
                            .setCameraId(0)
                            .setOrientationLocked(false)
                            .setBarcodeImageEnabled(true)
                            .setCaptureActivity(PortraitCaptureActivity::class.java)

                        scanLauncher.launch(options)
                    },
                    enabled = hasCameraPermission && hasLocationPermission,
                    modifier = Modifier.size(width = 200.dp, height = 60.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Scan QR Code"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan QR Code")
                    }
                }
            }
        }
    }
}

fun getUserLocation(
    context: Context,
    onLocationReceived: (Location) -> Unit,
    onLocationError: (String) -> Unit
) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasPermission) {
        onLocationError("Location permission not granted")
        return
    }

    try {
        // try to get GPS location first
        var location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        // fall back to network location
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }

        if (location != null) {
            onLocationReceived(location)
        } else {
            onLocationError("Unable to get your current location")
        }
    } catch (e: Exception) {
        Log.e("QRScannerScreen", "Error getting location", e)
        onLocationError("Error getting location: ${e.message}")
    }
}