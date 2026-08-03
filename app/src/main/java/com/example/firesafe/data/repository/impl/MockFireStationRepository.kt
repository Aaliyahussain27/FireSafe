package com.example.firesafe.data.repository.impl

import com.example.firesafe.data.model.FireStation
import com.example.firesafe.data.repository.FireStationRepository
import kotlinx.coroutines.delay

class MockFireStationRepository : FireStationRepository {
    override suspend fun getNearestStation(latitude: Double, longitude: Double): FireStation {
        delay(800) // Simulate API search delay
        return FireStation(
            name = "Connaught Place Fire Station, New Delhi",
            distanceKm = 1.2,
            etaMinutes = 4,
            responderName = "Duty Officer Rajesh Kumar",
            phoneNumber = "+911123412222"
        )
    }
}
