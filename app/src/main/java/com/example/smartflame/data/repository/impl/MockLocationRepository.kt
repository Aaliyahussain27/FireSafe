package com.example.smartflame.data.repository.impl

import com.example.smartflame.data.model.LocationData
import com.example.smartflame.data.repository.LocationRepository
import kotlinx.coroutines.delay

class MockLocationRepository : LocationRepository {
    override suspend fun getCurrentLocation(): LocationData {
        delay(1500) // Simulate GPS fix delay
        return LocationData(
            latitude = 37.7749,
            longitude = -122.4194,
            address = "850 Bryant St, San Francisco, CA 94103"
        )
    }
}
