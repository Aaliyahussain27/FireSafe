package com.example.firesafe.data.repository

import com.example.firesafe.data.model.AlertConfirmation

interface AlertRepository {
    suspend fun dispatchAlert(latitude: Double, longitude: Double, address: String): AlertConfirmation
    suspend fun cancelAlert(alertId: String): Boolean
}
