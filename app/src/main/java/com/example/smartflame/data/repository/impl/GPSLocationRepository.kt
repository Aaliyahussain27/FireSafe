package com.example.smartflame.data.repository.impl

import com.example.smartflame.data.model.LocationData
import com.example.smartflame.data.repository.LocationRepository

class GPSLocationRepository(private val context: android.content.Context) : LocationRepository {
    @android.annotation.SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): LocationData = kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        try {
            val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val address = try {
                            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            addresses?.firstOrNull()?.getAddressLine(0) ?: "${location.latitude}, ${location.longitude}"
                        } catch (e: Exception) {
                            "850 Bryant St, San Francisco, CA 94103 (GPS)"
                        }
                        if (continuation.isActive) {
                            continuation.resume(LocationData(location.latitude, location.longitude, address), onCancellation = null)
                        }
                    } else {
                        if (continuation.isActive) {
                            continuation.resume(LocationData(37.7749, -122.4194, "850 Bryant St, San Francisco, CA 94103 (GPS mock)"), onCancellation = null)
                        }
                    }
                }
                .addOnFailureListener {
                    if (continuation.isActive) {
                        continuation.resume(LocationData(37.7749, -122.4194, "850 Bryant St, San Francisco, CA 94103 (GPS error mock)"), onCancellation = null)
                    }
                }
        } catch (e: Exception) {
            if (continuation.isActive) {
                continuation.resume(LocationData(37.7749, -122.4194, "850 Bryant St, San Francisco, CA 94103 (GPS fallback)"), onCancellation = null)
            }
        }
    }
}
