package com.example.inf2007_mad_j1847.utils

object LocationData {
    val hospitalLocations = mapOf(
        "Sengkang Community Hospital" to Triple(1.3913, 103.8861, 200.0),
        "Singapore General Hospital" to Triple(1.2789, 103.8361, 200.0),
        "National University Hospital" to Triple(1.2936, 103.7833, 200.0),
        "Khoo Teck Puat Hospital" to Triple(1.4236, 103.8367, 200.0),
        "Mount Elizabeth Novena Hospital" to Triple(1.3205, 103.8453, 200.0),
        "Changi General Hospital" to Triple(1.3404, 103.9492, 200.0),
        "Dr Beef's Clinic" to Triple(1.403620, 103.893020, 500.0),
    )

    // fn to check if the user is at the hospital
    fun isUserAtHospital(hospitalName: String, userLat: Double, userLng: Double): Boolean {
        val hospitalInfo = hospitalLocations[hospitalName] ?: return false
        val (hospitalLat, hospitalLng, radius) = hospitalInfo

        // calc dist using haversine formula
        val earthRadius = 6371000.0 // in meters

        val latDistance = Math.toRadians(userLat - hospitalLat)
        val lngDistance = Math.toRadians(userLng - hospitalLng)

        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(hospitalLat)) * Math.cos(Math.toRadians(userLat)) *
                Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = earthRadius * c

        return distance <= radius
    }
}