package com.example.firesafe.data.repository

import com.example.firesafe.data.model.LocationData

interface LocationRepository {
    suspend fun getCurrentLocation(): LocationData
}
