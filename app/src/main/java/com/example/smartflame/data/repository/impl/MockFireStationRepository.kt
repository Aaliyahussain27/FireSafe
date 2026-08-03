package com.example.smartflame.data.repository.impl

import com.example.smartflame.data.model.FireStation
import com.example.smartflame.data.repository.FireStationRepository
import kotlinx.coroutines.delay

class MockFireStationRepository : FireStationRepository {
    override suspend fun getNearestStation(latitude: Double, longitude: Double): FireStation {
        delay(800) // Simulate API search delay
        return FireStation(
            name = "SFFD Station 1 (South of Market)",
            distanceKm = 1.2,
            etaMinutes = 4,
            responderName = "Captain Sarah Jenkins"
        )
    }
}
