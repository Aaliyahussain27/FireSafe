package com.example.smartflame.data.repository

import com.example.smartflame.data.model.LocationData

interface LocationRepository {
    suspend fun getCurrentLocation(): LocationData
}
