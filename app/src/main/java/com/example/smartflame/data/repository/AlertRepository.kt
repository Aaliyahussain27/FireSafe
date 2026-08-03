package com.example.smartflame.data.repository

import com.example.smartflame.data.model.AlertConfirmation

interface AlertRepository {
    suspend fun dispatchAlert(latitude: Double, longitude: Double, address: String): AlertConfirmation
    suspend fun cancelAlert(alertId: String): Boolean
}
