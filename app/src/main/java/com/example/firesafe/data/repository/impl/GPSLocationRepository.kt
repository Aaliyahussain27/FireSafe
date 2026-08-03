package com.example.firesafe.data.repository.impl

import com.example.firesafe.data.model.LocationData
import com.example.firesafe.data.repository.LocationRepository

class GPSLocationRepository(private val context: android.content.Context) : LocationRepository {
    @android.annotation.SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): LocationData = kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        try {
            // Ensure runtime permission check
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                if (continuation.isActive) {
                    continuation.resumeWith(Result.failure(SecurityException("ACCESS_FINE_LOCATION permission not granted")))
                }
                return@suspendCancellableCoroutine
            }

            val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val address = try {
                            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            addresses?.firstOrNull()?.getAddressLine(0) ?: "${location.latitude}, ${location.longitude}"
                        } catch (e: Exception) {
                            "${location.latitude}, ${location.longitude}"
                        }
                        if (continuation.isActive) {
                            continuation.resume(LocationData(location.latitude, location.longitude, address), onCancellation = null)
                        }
                    } else {
                        if (continuation.isActive) {
                            continuation.resumeWith(Result.failure(Exception("GPS returned null location")))
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) {
                        continuation.resumeWith(Result.failure(exception))
                    }
                }
        } catch (e: Exception) {
            if (continuation.isActive) {
                continuation.resumeWith(Result.failure(e))
            }
        }
    }
}
