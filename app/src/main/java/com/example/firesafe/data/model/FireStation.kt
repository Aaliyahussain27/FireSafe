package com.example.firesafe.data.model

data class FireStation(
    val name: String,
    val distanceKm: Double,
    val etaMinutes: Int,
    val responderName: String,
    val phoneNumber: String
)
