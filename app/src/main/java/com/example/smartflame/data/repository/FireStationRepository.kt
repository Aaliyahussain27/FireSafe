package com.example.smartflame.data.repository

import com.example.smartflame.data.model.FireStation

interface FireStationRepository {
    suspend fun getNearestStation(latitude: Double, longitude: Double): FireStation
}
